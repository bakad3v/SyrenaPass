package com.android.syrenapass.data.repositories

import android.content.Context
import androidx.datastore.dataStore
import com.android.syrenapass.data.serializers.SettingsSerializer
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.domain.repositories.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for changing app settings and enabling/disabling deletion
 */
class SettingsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context):SettingsRepository{

  private val Context.settingsDatastore by dataStore(DATASTORE_NAME, SettingsSerializer)

  companion object {
    private const val DATASTORE_NAME = "settings_datastore.json"
  }

  override val settings: Flow<Settings> = context.settingsDatastore.data

  override suspend fun setTheme(theme: Theme) {
    context.settingsDatastore.updateData {
      it.copy(theme = theme)
    }
  }

  override suspend fun setServiceStatus(working: Boolean) {
    context.settingsDatastore.updateData {
      it.copy(serviceWorking = working)
    }
  }

  override suspend fun setAdminStatus(isAdmin: Boolean) {
    context.settingsDatastore.updateData {
      it.copy(isAdmin = isAdmin)
    }
  }

  override suspend fun setActive() {
    context.settingsDatastore.updateData {
      it.copy(active = true)
    }
  }

  override suspend fun setInactive() {
    context.settingsDatastore.updateData {
      it.copy(active = false)
    }
  }
}
