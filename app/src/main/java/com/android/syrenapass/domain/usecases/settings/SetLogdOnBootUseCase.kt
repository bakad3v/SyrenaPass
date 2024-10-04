package com.android.syrenapass.domain.usecases.settings

import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetLogdOnBootUseCase @Inject constructor(private val repository: SettingsRepository) {
    suspend operator fun invoke(new: Boolean) {
        repository.setLogdOnBoot(new)
    }
}