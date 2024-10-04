package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetMultiuserUIUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke() {
        settingsRepository.enableMultiuserUI()
    }
}