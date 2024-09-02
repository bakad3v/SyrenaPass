package com.android.syrenapass.domain.usecases.admin

import android.os.Build
import androidx.annotation.RequiresApi
import javax.inject.Inject

class RemoveAppDataUseCase @Inject constructor(private val adminFunctions: AdminFunctions) {
  @RequiresApi(Build.VERSION_CODES.P)
  operator fun invoke(packageName: String) {
    adminFunctions.removeAppData(packageName)
  }
}
