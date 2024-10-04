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
import android.os.UserManager
import android.util.Log
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

class Owner @Inject constructor(@ApplicationContext private val context: Context, private val profilesMapper: ProfilesMapper, private val setOwnerActiveUseCase: SetOwnerActiveUseCase, private val appDPM: DevicePolicyManager, private val userManager: UserManager): SuperUser {

    private var initialized: Boolean = false
    private val dpm by lazy { getDhizukuDPM() }
    private val packageInstaller by lazy { getDhizukuPackageInstaller() }
    private val deviceOwner by lazy { Dhizuku.getOwnerComponent() }
    private val deviceAdmin by lazy { ComponentName(context, DeviceAdminReceiver::class.java) }

    private fun initDhizuku() {
        val init=Dhizuku.init(context)
        Log.w("initialized",init.toString())
        if (VERSION.SDK_INT >= Build.VERSION_CODES.P) HiddenApiBypass.setHiddenApiExemptions("")
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
        if (packageName == context.packageName && appDPM.isAdminActive(deviceAdmin)) {
            appDPM.removeActiveAdmin(deviceAdmin)
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


    fun askSuperUserRights(onApprove: () -> Unit, onDeny: () -> Unit, onAbsent: () -> Unit) {
        if (!initialized) {
            initDhizuku()
        }
        try {
            Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                override fun onRequestPermission(grantResult: Int) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        onApprove()
                    } else {
                        onDeny()
                    }
                }
            })
        } catch (e: AssertionError) {
            onAbsent()
        }
    }

    private fun checkOwner(): Boolean {
        return dpm.isDeviceOwnerApp("com.rosan.dhizuku") && dpm.isAdminActive(deviceOwner)
    }



    override suspend fun wipe() {
        var flags = 0
        if (VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            flags = flags.or(DevicePolicyManager.WIPE_SILENTLY)
        try {
            if (userManager.isSystemUser && VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                dpm.wipeDevice(flags)
            } else {
                dpm.wipeData(flags)
            }
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
            return userHandles.map { profilesMapper.mapUserHandleToProfile(it) }
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
            Log.w("error",e.stackTraceToString())
            handleException(e)
        }
    }

    override suspend fun hideApp(packageName: String) {
        try {
            dpm.setApplicationHidden(deviceOwner, packageName, true)
            dpm.addUserRestriction(deviceOwner,UserManager.DISALLOW_SAFE_BOOT)
        } catch (e: Exception) {
            Log.w("error",e.stackTraceToString())
            handleException(e)
        }
    }

    override suspend fun clearAppData(packageName: String) {
        try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.clearApplicationUserData(
                    deviceOwner,
                    packageName,
                    Executors.newSingleThreadExecutor()
                ) { packae, success -> }
            } else {
                throw SuperUserException(ANDROID_VERSION_INCORRECT.format("28"),UIText.StringResource(R.string.wrong_android_version,"28"))
            }
        } catch (e: Exception) {
            Log.w("error",e.stackTraceToString())
            handleException(e)
        }
    }

    override suspend fun runTrim() {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun executeRootCommand(command: String): Shell.Result {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun stopLogd() {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun enableMultiuserUI() {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun setUsersLimit(limit: Int) {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun getUserLimit(): Int? {
        throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
    }

    override suspend fun disableSafeBoot() {
        dpm.addUserRestriction(deviceOwner,UserManager.DISALLOW_SAFE_BOOT)
        if (VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            dpm.addUserRestrictionGlobally(UserManager.DISALLOW_SAFE_BOOT)
        }
    }

    companion object {
        private const val NO_OWNER_RIGHTS = "App doesn't have owner rights."
        private const val ANDROID_VERSION_INCORRECT = "Wrong android version, SDK version %s or higher required"
        private const val NO_ROOT_RIGHTS = "App doesn't have root rights"
    }

}