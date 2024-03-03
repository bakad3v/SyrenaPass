package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(private val repository: SettingsRepository) {
  operator fun invoke(): Flow<Settings> {
    return repository.settings
  }
}
