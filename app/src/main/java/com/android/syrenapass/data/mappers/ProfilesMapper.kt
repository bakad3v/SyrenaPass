package com.android.syrenapass.data.mappers

import android.content.Context
import android.content.Context.USER_SERVICE
import android.os.UserHandle
import android.os.UserManager
import com.android.syrenapass.data.db.ProfileDbModel
import com.android.syrenapass.domain.entities.ProfileDomain
import javax.inject.Inject

class ProfilesMapper @Inject constructor() {
    fun mapDbToDt(profileDbModel: ProfileDbModel): ProfileDomain = ProfileDomain(
        id = profileDbModel.id,
        name = profileDbModel.name,
        main = profileDbModel.main,
        toDelete = profileDbModel.toDelete
    )

    fun mapDtToDb(profileDomain: ProfileDomain) = ProfileDbModel(
        id = profileDomain.id,
        name = profileDomain.name,
        main = profileDomain.main,
        toDelete = profileDomain.toDelete
    )

    fun mapUserHandleToProfile(context: Context,userHandle: UserHandle): ProfileDomain {
        val userManager = context.getSystemService(USER_SERVICE) as UserManager
        val id = userManager.getSerialNumberForUser(userHandle).toInt()
        val main = id == 0
        return ProfileDomain(id,"Unknown name",main)
    }

    fun mapRootOutputToProfile(output: String) : ProfileDomain {
        val info = output.split("{")[1].split(":")
        val id = info[0].toInt()
        val name = info[1]
        return ProfileDomain(id = id,
            name = name,
            main = id == 0)
    }
}