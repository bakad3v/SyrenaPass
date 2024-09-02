package com.android.syrenapass.domain.usecases.admin

import javax.inject.Inject

class HideAppUseCase @Inject constructor(private val adminFunctions: AdminFunctions) {
  operator fun invoke(packageName: String) {
    adminFunctions.hideApp(packageName)
  }
}
