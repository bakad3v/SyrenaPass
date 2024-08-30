package com.android.syrenapass.data.repositories

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import com.android.syrenapass.domain.repositories.DeviceAdmin
import com.android.syrenapass.presentation.services.DeviceAdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject

class DeviceAdminImpl @Inject constructor(@ApplicationContext private val context: Context, private val dpm: DevicePolicyManager): DeviceAdmin {
  private val deviceAdmin by lazy { ComponentName(context, DeviceAdminReceiver::class.java) }

  override val isAdmin
    get() =  dpm.isAdminActive(deviceAdmin) ?: false

  override fun removeAdmin() {
    dpm.removeActiveAdmin(deviceAdmin)
  }

  @RequiresApi(Build.VERSION_CODES.P)
  override fun getUsers(): List<UserHandle> {
    return dpm.getSecondaryUsers(deviceAdmin) ?: listOf()
  }

  override fun requestAdminRights() =
    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
      .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin)


  override fun wipeData() {
    var flags = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
      flags = flags.or(DevicePolicyManager.WIPE_SILENTLY)
    dpm.wipeData(flags)
  }

  override fun removeUser(uid: Int) {
    dpm.removeUser(deviceAdmin, UserHandle.getUserHandleForUid(uid))
  }

  override fun hideApp(packageName: String) {
    dpm.setApplicationHidden(deviceAdmin,packageName,true)
  }

  @RequiresApi(Build.VERSION_CODES.P)
  override fun removeAppData(packageName: String) {
    dpm.clearApplicationUserData(deviceAdmin,packageName, Executors.newSingleThreadExecutor(), { packageN, succeeded ->  })
  }
}
