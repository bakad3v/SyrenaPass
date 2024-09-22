package com.android.syrenapass.domain.usecases.permissions

import com.android.syrenapass.domain.repositories.PermissionsRepository
import com.android.syrenapass.domain.repositories.SettingsRepository
import javax.inject.Inject

class SetOwnerActiveUseCase @Inject constructor(private val repository: PermissionsRepository) {
    suspend operator fun invoke(active: Boolean) {
        repository.setOwnerStatus(active)
    }
}