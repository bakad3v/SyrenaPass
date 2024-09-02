package com.android.syrenapass.data.repositories

import com.android.syrenapass.data.db.ProfileDAO
import com.android.syrenapass.data.mappers.ProfilesMapper
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.repositories.ProfilesRepository
import com.android.syrenapass.superuser.superuser.SuperUserException
import com.android.syrenapass.superuser.superuser.SuperUserManager
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.jvm.Throws

class ProfilesRepositoryImpl @Inject constructor(private val lazyProfilesDAO: Lazy<ProfileDAO>, private val profilesMapper: ProfilesMapper, private val superUserManager: SuperUserManager): ProfilesRepository {

    private lateinit var profilesDAO: ProfileDAO

    @Throws(SuperUserException::class)
    override suspend fun getProfiles(): Flow<List<ProfileDomain>> {
        loadAllProfiles()
        return profilesDAO.getProfiles().map { profileDbModels -> profileDbModels.map { profilesMapper.mapDbToDt(it) } }
    }

    override fun init() {
        profilesDAO = lazyProfilesDAO.get()
    }

    private suspend fun loadAllProfiles(){
       val currentProfiles = superUserManager.getSuperUser().getProfiles().map { profilesMapper.mapDtToDb(it) }
       val oldProfiles = profilesDAO.getProfiles().first()
       val removedProfilesIds = oldProfiles.map{it.id}.toSet().minus(currentProfiles.map { it.id }.toSet())
       removedProfilesIds.forEach {
           profilesDAO.delete(it)
       }
       currentProfiles.forEach {
           profilesDAO.insert(it)
       }
    }

    override suspend fun deleteProfile(id: Int) {
        profilesDAO.delete(id)
    }

    override suspend fun setProfileDeletionStatus(id: Int, status: Boolean) {
        profilesDAO.setDeletionStatus(id,status)
    }
}