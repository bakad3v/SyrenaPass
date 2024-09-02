package com.android.syrenapass.data.mappers

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.android.syrenapass.data.db.AppDbModel
import com.android.syrenapass.domain.entities.AppDomain
import javax.inject.Inject


class AppsMapper @Inject constructor(){
    fun mapPackageInfoToAppDT(context: Context, packageInfo: PackageInfo): AppDomain = AppDomain(
        packageName = packageInfo.packageName,
        appName = packageInfo.applicationInfo.name,
        system = isSystemApp(packageInfo.applicationInfo),
        enabled = packageInfo.applicationInfo.enabled,
        icon = packageInfo.applicationInfo.loadIcon(context.packageManager)
    )

    private fun isSystemApp(ai: ApplicationInfo): Boolean {
        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        return (ai.flags and mask) != 0
    }

    fun mapDtToDb(appDomain: AppDomain): AppDbModel = AppDbModel(
        packageName = appDomain.packageName,
        appName = appDomain.appName,
        system = appDomain.system,
        enabled = appDomain.enabled,
        toDelete = appDomain.toDelete,
        toHide = appDomain.toHide,
        toClearData = appDomain.toClearData
    )

    fun mapDbToDt(context: Context,appDbModel: AppDbModel): AppDomain = AppDomain(
        packageName = appDbModel.packageName,
        appName = appDbModel.appName,
        system = appDbModel.system,
        enabled = appDbModel.enabled,
        toHide = appDbModel.toHide,
        toDelete = appDbModel.toDelete,
        toClearData = appDbModel.toClearData,
        icon = context.packageManager.getPackageInfo(appDbModel.packageName, 0).applicationInfo.loadIcon(context.packageManager)
    )
}