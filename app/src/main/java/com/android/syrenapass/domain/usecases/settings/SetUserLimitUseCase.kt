package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetUserLimitUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(limit: Int) {
        repository.setUserLimit(limit)
    }
}