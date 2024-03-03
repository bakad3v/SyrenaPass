package com.android.syrenapass.domain.repositories

import kotlinx.coroutines.flow.Flow

interface PasswordManager {
  suspend fun setPassword(password: CharArray)
  suspend fun checkPassword(password: CharArray): Boolean
  suspend fun changePassword(password: String)
  val passwordStatus: Flow<Boolean>
}
