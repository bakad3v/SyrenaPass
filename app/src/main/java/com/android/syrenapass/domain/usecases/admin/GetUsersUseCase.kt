package com.android.syrenapass.domain.usecases.admin

import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import com.android.syrenapass.domain.repositories.DeviceAdmin
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(private val deviceAdmin: DeviceAdmin) {
  @RequiresApi(Build.VERSION_CODES.P)
  operator fun invoke(): List<UserHandle> {
    return deviceAdmin.getUsers()
  }
}
