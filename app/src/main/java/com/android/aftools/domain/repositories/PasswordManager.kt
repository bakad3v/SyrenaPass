package com.android.aftools.domain.repositories

import kotlinx.coroutines.flow.Flow

interface PasswordManager {
  suspend fun setPassword(password: CharArray)
  suspend fun checkPassword(password: CharArray): Boolean
  val passwordStatus: Flow<Boolean>
}
