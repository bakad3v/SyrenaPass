package com.android.syrenapass.domain.usecases.admin

import javax.inject.Inject

class WipeDataUseCase @Inject constructor(private val adminFunctions: AdminFunctions) {
  operator fun invoke() {
    adminFunctions.wipeData()
  }
}
