package com.android.syrenapass.superuser.owner

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.IDevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.IntentSender
import android.content.pm.IPackageInstaller
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Parcel
import com.android.syrenapass.R
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.usecases.permissions.SetOwnerActiveUseCase
import com.android.syrenapass.presentation.receivers.DeviceAdminReceiver
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUser
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.Dhizuku.binderWrapper
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.util.concurrent.Executors
import javax.inject.Inject

class Owner @Inject constructor(@ApplicationContext private val context: Context, private val profilesMapper: ProfilesMapper, private val setOwnerActiveUseCase: SetOwnerActiveUseCase): SuperUser {

    private var initialized: Boolean = false
    private val dpm by lazy { getDhizukuDPM() }
    private val packageInstaller by lazy { getDhizukuPackageInstaller() }
    private val deviceOwner by lazy { Dhizuku.getOwnerComponent() }

    private fun initDhizuku() {
        Dhizuku.init()
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        initialized = true
    }

    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    private fun getDhizukuDPM(): DevicePolicyManager {
        if (!initialized) {
            initDhizuku()
        }
        val dhizukuContext = context.createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val manager = dhizukuContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val field = manager.javaClass.getDeclaredField("mService")
        field.isAccessible = true
        val oldInterface = field[manager] as IDevicePolicyManager
        if (oldInterface is DhizukuBinderWrapper) return manager
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IDevicePolicyManager.Stub.asInterface(newBinder)
        field[manager] = newInterface
        return manager
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun getDhizukuPackageInstaller(): PackageInstaller {
        if (!initialized) {
            initDhizuku()
        }
        val context = context.createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val installer = context.packageManager.packageInstaller
        val field = installer.javaClass.getDeclaredField("mInstaller")
        field.isAccessible = true
        val oldInterface = field[installer] as IPackageInstaller
        if (oldInterface is DhizukuBinderWrapper) return installer
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IPackageInstaller.Stub.asInterface(newBinder)
        field[installer] = newInterface
        return installer
    }

    private fun checkAdminApp(packageName: String) {
        if (packageName == context.packageName && dpm.isAdminActive(deviceOwner)) {
            dpm.removeActiveAdmin(deviceOwner)
        }
    }


    private suspend fun handleException(e: Exception): Nothing {
        if (!checkOwner()) {
            setOwnerActiveUseCase(false)
            throw SuperUserException(NO_OWNER_RIGHTS, UIText.StringResource(R.string.no_admin_rights))
        }
        throw SuperUserException(e.stackTraceToString(),
            UIText.StringResource(R.string.unknow_owner_error,e.stackTraceToString()))
    }


    fun askSuperUserRights(onApprove: () -> Unit, onDeny: () -> Unit) {
        if (!initialized) {
            initDhizuku()
        }
        Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
            override fun onRequestPermission(grantResult: Int) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    onApprove()
                } else {
                    onDeny()
                }
            }
        })
    }

    private fun checkOwner(): Boolean {
        return dpm.isDeviceOwnerApp(context.packageName) && dpm.isAdminActive(deviceOwner)
    }

    override suspend fun wipe() {
        var flags = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            flags = flags.or(DevicePolicyManager.WIPE_SILENTLY)
        try {
            dpm.wipeData(flags)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun getProfiles(): List<ProfileDomain> {
        try {
            val userHandles =
                if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    dpm.getSecondaryUsers(deviceOwner) ?: listOf()
                } else {
                    throw SuperUserException(ANDROID_VERSION_INCORRECT.format("28"),UIText.StringResource(R.string.wrong_android_version,"28"))
                }
            return userHandles.map { profilesMapper.mapUserHandleToProfile(context, it) }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun removeProfile(id: Int) {
        try {
            dpm.removeUser(deviceOwner, profilesMapper.mapIdToUserHandle(id))
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun uninstallApp(packageName: String) {
        try {
            checkAdminApp(packageName)
            packageInstaller.uninstall(
                packageName,
                IntentSender.readIntentSenderOrNullFromParcel(Parcel.obtain())
            )
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun hideApp(packageName: String) {
        try {
            checkAdminApp(packageName)
            dpm.setApplicationHidden(deviceOwner, packageName, true)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun clearAppData(packageName: String) {
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                checkAdminApp(packageName)
                dpm.clearApplicationUserData(
                    deviceOwner,
                    packageName,
                    Executors.newSingleThreadExecutor()
                ) { _, _ -> }
            } else {
                throw SuperUserException(ANDROID_VERSION_INCORRECT.format("28"),UIText.StringResource(R.string.wrong_android_version,"28"))
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun runTrim() {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun executeRootCommand(command: String): Shell.Result {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    companion object {
        private const val NO_OWNER_RIGHTS = "App doesn't have owner rights."
        private const val ANDROID_VERSION_INCORRECT = "Wrong android version, SDK version %s or higher required"
        private const val NO_ROOT_RIGHTS = "App doesn't have root rights"
    }

}