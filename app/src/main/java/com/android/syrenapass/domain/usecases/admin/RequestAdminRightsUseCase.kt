package com.android.syrenapass.domain.usecases.admin

import android.content.Intent
import javax.inject.Inject

class RequestAdminRightsUseCase @Inject constructor(private val adminFunctions: AdminFunctions) {
  operator fun invoke(): Intent {
    return adminFunctions.requestAdminRights()
  }
}
