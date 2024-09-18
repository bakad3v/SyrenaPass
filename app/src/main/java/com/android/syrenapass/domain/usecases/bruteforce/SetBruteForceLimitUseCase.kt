package com.android.syrenapass.domain.usecases.bruteforce

import com.android.syrenapass.domain.entities.BruteforceSettings
import com.android.syrenapass.domain.repositories.BruteforceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetBruteForceLimitUseCase @Inject constructor(private val repository: BruteforceRepository) {
    suspend operator fun invoke(limit: Int) {
        repository.setBruteforceLimit(limit)
    }
}