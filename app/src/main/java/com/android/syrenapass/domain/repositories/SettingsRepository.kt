package com.android.syrenapass.domain.repositories

import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  val settings: Flow<Settings>

  suspend fun setTheme(theme: Theme)
  suspend fun setActive()
  suspend fun setInactive()
  suspend fun setServiceStatus(working: Boolean)

  suspend fun setAdminStatus(isAdmin: Boolean)
}
