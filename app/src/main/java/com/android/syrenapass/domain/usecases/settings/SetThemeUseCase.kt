package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(private val repository: SettingsRepository) {
  suspend operator fun invoke(theme: Theme) {
    repository.setTheme(theme)
  }
}
