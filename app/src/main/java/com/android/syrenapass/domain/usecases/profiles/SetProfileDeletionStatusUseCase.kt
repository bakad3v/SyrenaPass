package com.android.syrenapass.domain.usecases.profiles

import com.android.syrenapass.domain.repositories.ProfilesRepository
import javax.inject.Inject

class SetProfileDeletionStatusUseCase @Inject constructor(private val profilesRepository: ProfilesRepository) {
    suspend operator fun invoke(id: Int, status: Boolean) {
        return profilesRepository.setProfileDeletionStatus(id, status)
    }
}