package com.android.syrenapass.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.android.syrenapass.SyrenaApp
import com.android.syrenapass.domain.repositories.FilesRepository
import com.android.syrenapass.domain.repositories.LogsRepository
import com.android.syrenapass.domain.repositories.PasswordManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for handling operations with user password and encrypted database
 */
class PasswordManagerImpl @Inject constructor(
  @ApplicationContext private val context: Context,
  private val logsRepository: LogsRepository,
  private val filesRepository: FilesRepository
) : PasswordManager {
  private val Context.passwordStatus by preferencesDataStore(PREFERENCES_NAME)

  /**
   * Is encrypted database created?
   */
  override val passwordStatus = context.passwordStatus.data.map { preferences ->
    return@map preferences[KEY] ?: false
  }

  /**
   * Function for creating encrypted database and initializing all repositories with encrypted data
   */
  override suspend fun setPassword(password: CharArray) {
    SyrenaApp.createDatabase(context, password)
    context.passwordStatus.edit {
      it[KEY] = true
    }
    initRepositories()
  }

  /**
   * Function for initialization of all repositories
   */
  private suspend fun initRepositories() {
    filesRepository.init()
    logsRepository.init()
  }

  /**
   * Function for password validation and database unlocking
   */
  override suspend fun checkPassword(password: CharArray): Boolean {
    val db = SyrenaApp.createDatabase(context, password)
    return if (db != null) {
      initRepositories()
      true
    } else {
      false
    }
  }

  /**
   * Function for password changing
   */
  override suspend fun changePassword(password: String) {
    SyrenaApp.getDatabase()!!.query("PRAGMA rekey = '$password';", emptyArray())
  }

  companion object {
    private const val PREFERENCES_NAME = "Preferences"
    private val KEY = booleanPreferencesKey("password_set")
  }
}
