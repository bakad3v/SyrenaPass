package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetAdminStatusUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
  suspend operator fun invoke(isAdmin: Boolean) {
    settingsRepository.setAdminStatus(isAdmin)
  }
}
