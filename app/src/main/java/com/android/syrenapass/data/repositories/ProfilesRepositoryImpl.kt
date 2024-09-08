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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.jvm.Throws

class ProfilesRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, private val profilesMapper: ProfilesMapper, private val superUserManager: SuperUserManager, profilesSerializer: ProfilesSerializer): ProfilesRepository {

    private val Context.profilesDatastore by dataStoreDirectBootAware(
        DATASTORE_NAME,
        profilesSerializer
    )

    companion object {
        private const val DATASTORE_NAME = "profiles_datastore.json"
    }

    @Throws(SuperUserException::class)
    override suspend fun getProfiles(): Flow<PersistentList<ProfileDomain>> =
        context.profilesDatastore.data.map { it.list }


    private suspend fun loadAllProfiles() {
        val currentProfiles = superUserManager.getSuperUser().getProfiles()
        val oldProfiles = context.profilesDatastore.data.first().list
        val addedProfiles = currentProfiles.toSet().minus(oldProfiles.toSet())
        val removedProfilesIds =
            oldProfiles.map { it.id }.toSet().minus(currentProfiles.map { it.id }.toSet())
        context.profilesDatastore.updateData {
            it.deleteMultiple(removedProfilesIds)
            it.addMultiple(addedProfiles)
        }
    }

    override suspend fun deleteProfile(id: Int) {
        context.profilesDatastore.updateData { it.delete(id) }
    }

    override suspend fun setProfileDeletionStatus(id: Int, status: Boolean) {
        context.profilesDatastore.updateData { it.setDeletionStatus(id, status) }
    }
}

