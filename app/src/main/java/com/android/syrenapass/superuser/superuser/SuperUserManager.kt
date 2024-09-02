package com.android.syrenapass.superuser.superuser

import android.content.Intent
import com.android.syrenapass.R
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.admin.DeviceAdmin
import com.android.syrenapass.superuser.owner.Owner
import com.android.syrenapass.superuser.root.Root
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SuperUserManager @Inject constructor(private val owner: Owner, private val root: Root, private val admin: DeviceAdmin, private val getSettingsUseCase: GetSettingsUseCase){

    fun askRootRights(): Boolean = root.askSuperUserRights()

    fun askDeviceOwnerRights(): Intent = owner.askSuperUserRights()

    fun askDeviceAdminRights(): Intent = admin.askSuperUserRights()

    suspend fun getSuperUser(): SuperUser {
        val settings = getSettingsUseCase().first()
        if (settings.isRoot)
            return root
        if (settings.isOwner)
            return owner
        if (settings.isAdmin)
            return admin
        throw SuperUserException(NO_SUPERUSER_RIGHTS,UIText.StringResource(R.string.no_superuser_rights))
    }

    companion object {
        private const val NO_SUPERUSER_RIGHTS = "You don't have superuser rights"
    }
}