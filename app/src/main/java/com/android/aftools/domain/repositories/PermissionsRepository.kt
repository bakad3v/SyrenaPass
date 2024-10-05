package com.android.aftools.domain.repositories

import com.android.aftools.domain.entities.Permissions
import kotlinx.coroutines.flow.Flow

interface PermissionsRepository {
    suspend fun setAdminStatus(status: Boolean)
    suspend fun setOwnerStatus(status: Boolean)
    suspend fun setRootStatus(status: Boolean)
    val permissions: Flow<Permissions>
}