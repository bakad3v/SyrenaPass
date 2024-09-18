package com.android.syrenapass.domain.usecases.bruteforce

import com.android.syrenapass.domain.repositories.BruteforceRepository
import javax.inject.Inject

class OnRightPasswordUseCase @Inject constructor(private val repository: BruteforceRepository) {
    suspend operator fun invoke() {
        repository.onRightPassword()
    }
}