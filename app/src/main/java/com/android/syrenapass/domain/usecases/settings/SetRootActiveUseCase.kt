package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetRootActiveUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke() {
        repository.setRootStatus(true)
    }
}
