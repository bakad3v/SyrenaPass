package com.android.syrenapass.domain.usecases.permissions

import com.android.syrenapass.domain.entities.Permissions
import com.android.syrenapass.domain.repositories.PermissionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPermissionsUseCase @Inject constructor(private val repository: PermissionsRepository) {
    operator fun invoke(): Flow<Permissions> {
        return repository.permissions
    }
}