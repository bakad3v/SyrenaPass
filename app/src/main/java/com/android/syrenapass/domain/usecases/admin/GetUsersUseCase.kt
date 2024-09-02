package com.android.syrenapass.domain.usecases.admin

import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(private val adminFunctions: AdminFunctions) {
  @RequiresApi(Build.VERSION_CODES.P)
  operator fun invoke(): List<UserHandle> {
    return adminFunctions.getUsers()
  }
}
