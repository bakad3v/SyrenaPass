package com.android.syrenapass.superuser.root

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager
import android.util.Log
import com.android.syrenapass.R
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.usecases.permissions.SetRootActiveUseCase
import com.android.syrenapass.presentation.receivers.DeviceAdminReceiver
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUser
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.qualifiers.ApplicationContext

import javax.inject.Inject

class Root @Inject constructor(@ApplicationContext private val context: Context, private val profilesMapper: ProfilesMapper, private val setRootActiveUseCase: SetRootActiveUseCase, private val dpm: DevicePolicyManager, private val userManager: UserManager) : SuperUser {

    private val deviceAdmin by lazy { ComponentName(context, DeviceAdminReceiver::class.java) }

    override suspend fun executeRootCommand(command: String): Shell.Result {
        val result = Shell.cmd(command).exec()
        if (!result.isSuccess) {
            if (!askSuperUserRights()) {
                setRootActiveUseCase(false)
                throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
            }
            val resultText = result.out.joinToString(";")
            throw SuperUserException(resultText,UIText.StringResource(R.string.unknow_root_error,resultText))
        }
        return result
    }

    private suspend fun checkAdminApp(packageName: String) {
        if (packageName == context.packageName && dpm.isAdminActive(deviceAdmin)) {
            executeRootCommand("dpm remove-active-admin ${context.packageName}/${deviceAdmin.shortClassName}")
        }
    }

    override suspend fun uninstallApp(packageName: String) {
        checkAdminApp(packageName)
        executeRootCommand("pm uninstall $packageName")
    }

    override suspend fun hideApp(packageName: String) {
        checkAdminApp(packageName)
        executeRootCommand("pm disable $packageName")
    }

    override suspend fun clearAppData(packageName: String) {
        checkAdminApp(packageName)
        executeRootCommand("pm clear $packageName")
    }

    override suspend fun removeProfile(id: Int) {
        executeRootCommand("pm remove-user $id")
    }

    override suspend fun stopLogd() {
        executeRootCommand("stop logd")
    }

    override suspend fun enableMultiuserUI() {
        executeRootCommand("setprop fw.show_multiuserui 1")
    }

    override suspend fun setUsersLimit(limit: Int) {
        executeRootCommand("setprop fw.max_users $limit")
    }

    override suspend fun getUserLimit(): Int? {
        return Regex("\\d").find(executeRootCommand("pm get-max-users").out[0])?.value?.toInt()
    }

    override suspend fun disableSafeBoot() {
        executeRootCommand("pm set-user-restriction ${UserManager.DISALLOW_SAFE_BOOT} 1")
    }

    fun askSuperUserRights(): Boolean {
        val result = Shell.cmd("id").exec()
        Log.w("uid=0(root)",result.out.joinToString { ";" })
        return result.out[0].startsWith("uid=0(root)")
    }

    override suspend fun wipe() {
        if (userManager.isSystemUser) {
            executeRootCommand("recovery --wipe_data")
        } else {
            executeRootCommand("am broadcast -a android.intent.action.MASTER_CLEAR -n android/com.android.server.MasterClearReceiver")
        }
    }

    override suspend fun getProfiles(): List<ProfileDomain> {
        val result = executeRootCommand("pm list users")
        return result.out.drop(1).map { profilesMapper.mapRootOutputToProfile(it) }
    }

    override suspend fun runTrim() {
        executeRootCommand("sm fstrim")
    }

  companion object {
      private const val NO_ROOT_RIGHTS = "App doesn't have root rights"
  }
}