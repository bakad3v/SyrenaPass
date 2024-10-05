package com.android.aftools.data.repositories

import android.content.Context
import com.android.aftools.data.serializers.PasswordStatusSerializer
import com.android.aftools.datastoreDBA.dataStoreDirectBootAware
import com.android.aftools.domain.entities.PasswordStatus
import com.android.aftools.domain.repositories.PasswordManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for handling operations with user password and encrypted database
 */
class PasswordManagerImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  passwordStatusSerializer: PasswordStatusSerializer
) : PasswordManager {
  private val Context.passwordPrefs by dataStoreDirectBootAware(PREFERENCES_NAME,passwordStatusSerializer)

  /**
   * Is encrypted database created?
   */
  override val passwordStatus = context.passwordPrefs.data.map { preferences ->
    preferences.passwordSet
  }


  /**
   * Function for creating password
   */
  override suspend fun setPassword(password: CharArray) {
    context.passwordPrefs.updateData {
      PasswordStatus(password = password.concatToString(), passwordSet = true)
    }
    password.clear()
  }

  /**
   * Function for password validation
   */
  override suspend fun checkPassword(password: CharArray): Boolean {
    val currentPassword = context.passwordPrefs.data.first().password
    val rightPassword = password.concatToString() == currentPassword
    password.clear()
    return rightPassword
  }

  private fun CharArray.clear() {
    for (i in indices) {
      this[i] = '\u0000'
    }
  }

  companion object {
    private const val PREFERENCES_NAME = "password_preferences"
  }
}
