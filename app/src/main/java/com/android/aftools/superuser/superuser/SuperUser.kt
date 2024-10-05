package com.android.aftools.superuser.superuser

import com.android.aftools.domain.entities.ProfileDomain
import com.topjohnwu.superuser.Shell
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
    @Throws(SuperUserException::class)
    suspend fun runTrim()
    @Throws(SuperUserException::class)
    suspend fun executeRootCommand(command: String): Shell.Result
    @Throws(SuperUserException::class)
    suspend fun stopLogd()
    @Throws(SuperUserException::class)
    suspend fun enableMultiuserUI()
    @Throws(SuperUserException::class)
    suspend fun setUsersLimit(limit: Int)
    @Throws(SuperUserException::class)
    suspend fun getUserLimit(): Int?
    @Throws
    suspend fun disableSafeBoot()
}