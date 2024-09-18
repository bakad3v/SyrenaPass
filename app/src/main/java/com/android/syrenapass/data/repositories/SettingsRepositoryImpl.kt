package com.android.syrenapass.data.repositories

import android.content.Context
import com.android.syrenapass.data.serializers.SettingsSerializer
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.domain.repositories.SettingsRepository
import com.android.syrenapass.datastoreDBA.dataStoreDirectBootAware
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for changing app settings and enabling/disabling deletion
 */
class SettingsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, settingsSerializer: SettingsSerializer):SettingsRepository{

  private val Context.settingsDatastore by dataStoreDirectBootAware(DATASTORE_NAME, settingsSerializer)
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

  override suspend fun setRunOnBoot(status: Boolean) {
    context.settingsDatastore.updateData {
      it.copy(runOnBoot = status)
    }
  }


}
