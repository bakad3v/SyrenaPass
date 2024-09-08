package com.android.syrenapass.data.repositories

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import com.android.syrenapass.TopLevelFunctions.toByteArray
import com.android.syrenapass.data.encryption.EncryptionManager
import com.android.syrenapass.domain.repositories.PasswordManager
import com.android.syrenapass.datastoreDBA.preferencesDataStoreDirectBootAware
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.signal.argon2.Argon2
import javax.inject.Inject

/**
 * Repository for handling operations with user password and encrypted database
 */
class PasswordManagerImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val argon2: Argon2,
  private val encryptionManager: EncryptionManager
) : PasswordManager {
  private val Context.passwordPrefs by preferencesDataStoreDirectBootAware(PREFERENCES_NAME)

  /**
   * Is encrypted database created?
   */
  override val passwordStatus = context.passwordPrefs.data.map { preferences ->
    return@map preferences[STATUS_KEY] ?: false
  }

  private val passwordHash = context.passwordPrefs.data.map { preferences ->
    return@map preferences[PASSWORD_HASH_KEY]
  }

  private val passwordSalt = context.passwordPrefs.data.map { preferences ->
    return@map preferences[PASSWORD_SALT_KEY]
  }

  /**
   * Function for creating encrypted database and initializing all repositories with encrypted data
   */
  override suspend fun setPassword(password: CharArray) {
    val salt = encryptionManager.getSalt()
    Log.w("salt",Base64.encodeToString(salt,Base64.DEFAULT))
    val hash = argon2.hash(password.toByteArray(),salt)
    context.passwordPrefs.edit {
      it[STATUS_KEY] = true
      it[PASSWORD_SALT_KEY] = salt
      it[PASSWORD_HASH_KEY] = hash.hash
    }
    password.clear()
  }

  /**
   * Function for password validation and database unlocking
   */
  override suspend fun checkPassword(password: CharArray): Boolean {
    val hash = argon2.hash(password.toByteArray(),passwordSalt.first()).hash
    password.clear()
    Log.w("password",hash.contentEquals(passwordHash.first()).toString())
    return hash.contentEquals(passwordHash.first())
  }

  private fun CharArray.clear() {
    for (i in indices) {
      this[i] = '\u0000'
    }
  }

  companion object {
    private const val PREFERENCES_NAME = "Preferences"
    private val STATUS_KEY = booleanPreferencesKey("password_set")
    private val PASSWORD_HASH_KEY = byteArrayPreferencesKey("password_hash")
    private val PASSWORD_SALT_KEY = byteArrayPreferencesKey("password_salt")
  }
}
