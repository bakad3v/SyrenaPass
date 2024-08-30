package com.android.syrenapass.domain.usecases.admin

import com.android.syrenapass.domain.repositories.DeviceAdmin
import javax.inject.Inject

class WipeDataUseCase @Inject constructor(private val deviceAdmin: DeviceAdmin) {
  operator fun invoke() {
    deviceAdmin.wipeData()
  }
}
