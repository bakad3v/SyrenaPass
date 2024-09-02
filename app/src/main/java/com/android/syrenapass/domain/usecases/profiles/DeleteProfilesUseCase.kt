package com.android.syrenapass.domain.usecases.profiles

import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.repositories.ProfilesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteProfilesUseCase @Inject constructor(private val profilesRepository: ProfilesRepository) {
    suspend operator fun invoke(id: Int) {
        return profilesRepository.deleteProfile(id)
    }
}