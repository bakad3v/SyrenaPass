package com.android.syrenapass.superuser.owner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.Parcel
import android.os.UserHandle
import com.android.syrenapass.R
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.usecases.settings.SetOwnerInactiveUseCase
import com.android.syrenapass.presentation.services.DeviceAdminReceiver
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUser
import com.android.syrenapass.superuser.superuser.SuperUserException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject

class Owner @Inject constructor(@ApplicationContext private val context: Context, private val dpm: DevicePolicyManager,private val profilesMapper: ProfilesMapper, private val setOwnerInactiveUseCase: SetOwnerInactiveUseCase): SuperUser {

    private val deviceOwner by lazy { ComponentName(context, DeviceAdminReceiver::class.java) }

    private suspend fun handleException(e: Exception): Nothing {
        if (!checkOwner()) {
            setOwnerInactiveUseCase()
            throw SuperUserException(NO_OWNER_RIGHTS, UIText.StringResource(R.string.no_admin_rights))
        }
        throw SuperUserException(e.stackTraceToString(),
            UIText.StringResource(R.string.unknow_owner_error,e.stackTraceToString()))
    }


    fun askSuperUserRights(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(
            context,
            DeviceAdminReceiver::class.java
        ))
        return intent
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
            dpm.removeUser(deviceOwner, UserHandle.getUserHandleForUid(id))
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun uninstallApp(packageName: String) {
        try {
            context.packageManager.packageInstaller.uninstall(
                packageName,
                IntentSender.readIntentSenderOrNullFromParcel(Parcel.obtain())
            )
        } catch (e: Exception) {
            handleException(e)
        }
        //probably incorrect
    }

    override suspend fun hideApp(packageName: String) {
        try {
            dpm.setApplicationHidden(deviceOwner, packageName, true)
        } catch (e: Exception) {
            handleException(e)
        }
    }

    override suspend fun clearAppData(packageName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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

    companion object {
        private const val NO_OWNER_RIGHTS = "App doesn't have owner rights."
        private const val ANDROID_VERSION_INCORRECT = "Wrong android version, SDK version %s or higher required"
    }

}