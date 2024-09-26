package com.android.syrenapass.data.mappers

import android.content.Context
import android.content.Context.USER_SERVICE
import android.os.Parcel
import android.os.UserHandle
import android.os.UserManager
import android.util.Log
import com.android.syrenapass.data.entities.IntList
import com.android.syrenapass.domain.entities.ProfileDomain
import javax.inject.Inject

class ProfilesMapper @Inject constructor() {

    fun mapUserHandleToProfile(context: Context,userHandle: UserHandle): ProfileDomain {
        val userManager = context.getSystemService(USER_SERVICE) as UserManager
        val id = userManager.getSerialNumberForUser(userHandle).toInt()
        val main = id == 0
        return ProfileDomain(id,"Unknown name",main)
    }

    fun mapToProfilesWithStatus(profiles: List<ProfileDomain>?, ids: IntList): List<ProfileDomain>? =
        profiles?.map {
            if (it.id in ids.list) {
                it.copy(toDelete = true)
            } else {
                it
            }
        }

    fun mapIdToUserHandle(id: Int): UserHandle {
        val parcel = Parcel.obtain()
        parcel.writeInt(id)
        parcel.setDataPosition(0)
        val user = UserHandle.readFromParcel(parcel)
        parcel.recycle()
        return user
    }

    fun mapRootOutputToProfile(output: String) : ProfileDomain {
        Log.w("output",output)
        val info = output.split("{")[1].split(":")
        val id = info[0].toInt()
        val name = info[1]
        return ProfileDomain(id = id,
            name = name,
            main = id == 0)
    }
}