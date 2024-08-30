package com.android.syrenapass.domain.usecases.admin

import com.android.syrenapass.domain.repositories.DeviceAdmin
import javax.inject.Inject

class HideAppUseCase @Inject constructor(private val deviceAdmin: DeviceAdmin) {
  operator fun invoke(packageName: String) {
    deviceAdmin.hideApp(packageName)
  }
}
