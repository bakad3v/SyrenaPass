package com.android.syrenapass.domain.usecases.admin

import android.content.Intent
import com.android.syrenapass.domain.repositories.DeviceAdmin
import javax.inject.Inject

class RequestAdminRightsUseCase @Inject constructor(private val deviceAdmin: DeviceAdmin) {
  operator fun invoke(): Intent {
    return deviceAdmin.requestAdminRights()
  }
}
