package com.android.syrenapass.domain.repositories

import android.content.Context
import com.android.syrenapass.domain.entities.AppDomain
import kotlinx.coroutines.flow.Flow

interface AppsRepository {
    fun getInstalledApplications(): List<AppDomain>
    suspend fun addApplications(apps: List<AppDomain>)
    suspend fun removeApplication(packageName: String)
    suspend fun clearDb()
    fun init()
    suspend fun setDeletionStatus(status: Boolean, packageName: String)
    suspend fun setHiddenStatus(status: Boolean, packageName: String)
    suspend fun setDataClearStatus(status: Boolean, packageName: String)
    fun getManagedApps(): Flow<List<AppDomain>>
}