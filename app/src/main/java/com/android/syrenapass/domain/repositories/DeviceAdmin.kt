package com.android.syrenapass.domain.repositories

import android.content.Intent
import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi

interface DeviceAdmin {
  val isAdmin: Boolean

  @RequiresApi(Build.VERSION_CODES.P)
  fun removeAppData(packageName: String)
  fun hideApp(packageName: String)
  fun removeUser(uid: Int)
  fun wipeData()
  fun requestAdminRights(): Intent
  fun removeAdmin()
  @RequiresApi(Build.VERSION_CODES.P)
  fun getUsers(): List<UserHandle>
}
