package com.android.syrenapass.domain.usecases.admin

import javax.inject.Inject

class RemoveUserUseCase @Inject constructor(private val adminFunctions: AdminFunctions) {
  operator fun invoke(uid: Int) {
    adminFunctions.removeUser(uid)
  }
}
