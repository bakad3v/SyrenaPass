package com.android.syrenapass.data.repositories

import android.content.Context
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.data.serializers.ProfilesSerializer
import com.android.syrenapass.datastoreDBA.dataStoreDirectBootAware
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.repositories.ProfilesRepository
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.android.syrenapass.superuser.superuser.SuperUserManager
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.jvm.Throws

class ProfilesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val profilesMapper: ProfilesMapper,
    private val superUserManager: SuperUserManager,
    private val profilesOnDevice: MutableStateFlow<List<ProfileDomain>>,
    private val coroutineScope: CoroutineScope,
    profilesSerializer: ProfilesSerializer
) : ProfilesRepository {

    private val Context.profilesDatastore by dataStoreDirectBootAware(
        DATASTORE_NAME,
        profilesSerializer
    )

    companion object {
        private const val DATASTORE_NAME = "profiles_datastore.json"
    }



    @Throws(SuperUserException::class)
    override fun getProfiles(): Flow<List<ProfileDomain>> {
        coroutineScope.launch {
            refreshDeviceProfiles()
        }
        return combine(context.profilesDatastore.data, profilesOnDevice) { toDelete, profiles -> profilesMapper.mapToProfilesWithStatus(profiles,toDelete)}
    }

    override suspend fun refreshDeviceProfiles() {
        profilesOnDevice.emit(
            superUserManager.getSuperUser().getProfiles()
        )
    }

    override fun getProfilesToDelete() : Flow<List<Int>> = context.profilesDatastore.data.map { it.list }

    override suspend fun deleteProfile(id: Int) {
        context.profilesDatastore.updateData { it.delete(id) }
    }

    override suspend fun setProfileDeletionStatus(id: Int, status: Boolean) {
        context.profilesDatastore.updateData {
            if (status) {
                it.add(id)
            } else {
                it.delete(id)
            }
        }
    }
}

