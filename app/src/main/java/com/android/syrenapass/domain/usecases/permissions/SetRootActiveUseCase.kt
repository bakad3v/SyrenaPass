package com.android.syrenapass.domain.usecases.permissions

import com.android.syrenapass.domain.repositories.PermissionsRepository
import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetRootActiveUseCase @Inject constructor(private val repository: PermissionsRepository) {
    suspend operator fun invoke(status: Boolean) {
        repository.setRootStatus(status)
    }
}