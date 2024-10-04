package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetLogdOnStartUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(new: Boolean) {
        repository.setLogdOnStart(new)
    }
}