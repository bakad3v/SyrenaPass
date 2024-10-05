package com.android.aftools.domain.repositories

import com.android.aftools.domain.entities.ProfileDomain
import kotlinx.coroutines.flow.Flow

interface ProfilesRepository {
    fun getProfiles(): Flow<List<ProfileDomain>?>
    suspend fun deleteProfile(id: Int)
    suspend fun setProfileDeletionStatus(id: Int, status: Boolean)
    fun getProfilesToDelete(): Flow<List<Int>>
    suspend fun refreshDeviceProfiles()
}