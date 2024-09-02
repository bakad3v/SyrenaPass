package com.android.syrenapass.domain.usecases.profiles

import com.android.syrenapass.domain.repositories.ProfilesRepository
import javax.inject.Inject

class MarkProfileForDeletionUseCase @Inject constructor(private val profilesRepository: ProfilesRepository) {
    suspend operator fun invoke(id: Int) {
        return profilesRepository.setProfileDeletionStatus(id,true)
    }
}