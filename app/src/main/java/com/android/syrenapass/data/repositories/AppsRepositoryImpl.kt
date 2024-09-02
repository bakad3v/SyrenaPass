package com.android.syrenapass.data.repositories

import android.content.Context
import com.android.syrenapass.data.db.AppDao
import com.android.syrenapass.data.db.FileDao
import com.android.syrenapass.data.db.ProfileDAO
import com.android.syrenapass.data.mappers.AppsMapper
import com.android.syrenapass.domain.entities.AppDomain
import com.android.syrenapass.domain.repositories.AppsRepository
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, private val appsMapper: AppsMapper, private val lazyAppsDao: Lazy<AppDao>): AppsRepository {

    private lateinit var appsDao: AppDao

    override fun init() {
        appsDao = lazyAppsDao.get()
    }

    override fun getManagedApps() : Flow<List<AppDomain>> = appsDao.getAppsSortedByNameAsc().map { appDbModels -> appDbModels.map {appsMapper.mapDbToDt(context,it)} }

    override fun getInstalledApplications(): List<AppDomain> {
        val installedApps: List<AppDomain> = context.packageManager.getInstalledPackages(0).map{ appsMapper.mapPackageInfoToAppDT(context,it)}
        return installedApps
    }

    override suspend fun addApplications(apps: List<AppDomain>) {
        apps.forEach {
            appsDao.upsert(appsMapper.mapDtToDb(it))
        }
    }

    override suspend fun setDeletionStatus(status: Boolean, packageName: String) {
        appsDao.setDeletionStatus(status,packageName)
    }

    override suspend fun setHiddenStatus(status: Boolean, packageName: String) {
        appsDao.setHiddenStatus(status,packageName)
    }

    override suspend fun setDataClearStatus(status: Boolean, packageName: String) {
        appsDao.setClearedStatus(status,packageName)
    }

    override suspend fun removeApplication(packageName: String) {
        appsDao.delete(packageName)
    }

    override suspend fun clearDb() {
        appsDao.clearDb()
    }

}