package com.android.syrenapass.domain.usecases.profiles

import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.repositories.ProfilesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfilesUseCase @Inject constructor(private val profilesRepository: ProfilesRepository) {
    suspend operator fun invoke(): Flow<List<ProfileDomain>> {
        return profilesRepository.getProfiles()
    }
}