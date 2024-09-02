package com.android.syrenapass.superuser.root

import com.android.syrenapass.R
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.usecases.settings.SetRootInactiveUseCase
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUser
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.topjohnwu.superuser.Shell

import javax.inject.Inject

class Root @Inject constructor(private val profilesMapper: ProfilesMapper, private val setRootInactiveUseCase: SetRootInactiveUseCase) : SuperUser {

    private suspend fun executeRoot(command: String): Shell.Result {
        val result = Shell.cmd(command).exec()
        if (!result.isSuccess) {
            if (!askSuperUserRights()) {
                setRootInactiveUseCase()
                throw SuperUserException(NO_ROOT_RIGHTS,UIText.StringResource(R.string.no_root_rights))
            }
            val resultText = result.out.joinToString("\n")
            throw SuperUserException(resultText,UIText.StringResource(R.string.unknow_root_error,resultText))
        }
        return result
    }

    override suspend fun uninstallApp(packageName: String) {
        executeRoot("pm uninstall $packageName")
    }

    override suspend fun hideApp(packageName: String) {
        executeRoot("pm disable $packageName")
    }

    override suspend fun clearAppData(packageName: String) {
        executeRoot("pm clear $packageName")
    }

    override suspend fun removeProfile(id: Int) {
        executeRoot("pm remove-user $id")
    }

    fun askSuperUserRights(): Boolean {
        val result = Shell.cmd("id").exec()
        return result.isSuccess
    }

    override suspend fun wipe() {
        executeRoot("recovery --wipe_data")
    }

    override suspend fun getProfiles(): List<ProfileDomain> {
        val result = executeRoot("pm list-users")
        return result.out.map { profilesMapper.mapRootOutputToProfile(it) }
    }

  companion object {
      private const val NO_ROOT_RIGHTS = "App doesn't have root rights"
  }
}