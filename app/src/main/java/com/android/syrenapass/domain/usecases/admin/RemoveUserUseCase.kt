package com.android.syrenapass.domain.usecases.admin

import com.android.syrenapass.domain.repositories.DeviceAdmin
import javax.inject.Inject

class RemoveUserUseCase @Inject constructor(private val deviceAdmin: DeviceAdmin) {
  operator fun invoke(uid: Int) {
    deviceAdmin.removeUser(uid)
  }
}
