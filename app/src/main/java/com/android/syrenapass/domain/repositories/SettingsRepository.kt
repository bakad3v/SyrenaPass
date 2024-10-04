package com.android.syrenapass.domain.repositories

import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  val settings: Flow<Settings>

  suspend fun setTheme(theme: Theme)
  suspend fun setServiceStatus(working: Boolean)
  suspend fun setRunOnBoot(status: Boolean)
    suspend fun setDeleteApps(new: Boolean)
  suspend fun setDeleteFiles(new: Boolean)
  suspend fun setDeleteProfiles(new: Boolean)
  suspend fun setTRIM(new: Boolean)
  suspend fun setWipe(new: Boolean)
  suspend fun runRoot(new: Boolean)
  suspend fun sendBroadcast(new: Boolean)
  suspend fun setRemoveItself(new: Boolean)
    suspend fun setLogdOnStart(new: Boolean)
  suspend fun setLogdOnBoot(new: Boolean)
  suspend fun setClearAndHide(new: Boolean)
    suspend fun setUserLimit(limit: Int)
  suspend fun enableMultiuserUI()
    suspend fun getUserLimit(): Int?
    suspend fun disableSafeBoot()
}
