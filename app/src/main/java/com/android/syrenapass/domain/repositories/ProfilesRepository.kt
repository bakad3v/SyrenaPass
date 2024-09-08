package com.android.syrenapass.domain.repositories

import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.entities.SuperUserStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface ProfilesRepository {
    suspend fun getProfiles(): Flow<List<ProfileDomain>>
    suspend fun deleteProfile(id: Int)
    suspend fun setProfileDeletionStatus(id: Int, status: Boolean)
}