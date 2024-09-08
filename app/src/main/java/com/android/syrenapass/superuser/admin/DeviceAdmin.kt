package com.android.syrenapass.superuser.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.usecases.settings.SetAdminInactiveUseCase
import com.android.syrenapass.presentation.services.DeviceAdminReceiver
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUser
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class DeviceAdmin @Inject constructor(@ApplicationContext private val context: Context, private val dpm: DevicePolicyManager, private val setAdminInactiveUseCase: SetAdminInactiveUseCase): SuperUser {
    private val deviceAdmin by lazy { ComponentName(context, DeviceAdminReceiver::class.java) }

    fun askSuperUserRights(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(
            context,
            DeviceAdminReceiver::class.java
        ))
        return intent
    }

    private fun checkAdminRights(): Boolean = dpm.isAdminActive(deviceAdmin)

    private suspend fun handleException(e: Exception) {
        if (!checkAdminRights()) {
            setAdminInactiveUseCase()
            throw SuperUserException(NO_ADMIN_RIGHTS,UIText.StringResource(R.string.no_admin_rights))
        }
        throw SuperUserException(e.stackTraceToString(),UIText.StringResource(R.string.unknow_admin_error,e.stackTraceToString()))
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
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    override suspend fun removeProfile(id: Int) {
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    override suspend fun uninstallApp(packageName: String) {
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    override suspend fun hideApp(packageName: String) {
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    override suspend fun clearAppData(packageName: String) {
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    override suspend fun runTrim() {
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    override suspend fun executeRootCommand(command: String): Shell.Result {
        throw SuperUserException(ADMIN_ERROR_TEXT,UIText.StringResource(R.string.device_admin_error))
    }

    companion object {
        private const val ADMIN_ERROR_TEXT = "Device admin rights are not enough to perform operations."
        private const val NO_ADMIN_RIGHTS = "App doesn't have admin rights."
    }
}