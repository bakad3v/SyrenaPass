package com.android.syrenapass.superuser.superuser

import com.android.syrenapass.domain.entities.ProfileDomain
import kotlin.jvm.Throws

interface SuperUser {

    @Throws(SuperUserException::class)
    suspend fun wipe()
    @Throws(SuperUserException::class)
    suspend fun getProfiles(): List<ProfileDomain>
    @Throws(SuperUserException::class)
    suspend fun removeProfile(id: Int)
    @Throws(SuperUserException::class)
    suspend fun uninstallApp(packageName: String)
    @Throws(SuperUserException::class)
    suspend fun hideApp(packageName: String)
    @Throws(SuperUserException::class)
    suspend fun clearAppData(packageName: String)
}