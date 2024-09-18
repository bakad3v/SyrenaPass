package com.android.syrenapass.domain.usecases.bruteforce

import com.android.syrenapass.domain.entities.BruteforceSettings
import com.android.syrenapass.domain.entities.Permissions
import com.android.syrenapass.domain.repositories.BruteforceRepository
import com.android.syrenapass.domain.repositories.PermissionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBruteforceSettingsUseCase @Inject constructor(private val repository: BruteforceRepository) {
    operator fun invoke(): Flow<BruteforceSettings> {
        return repository.bruteforceSettings
    }
}