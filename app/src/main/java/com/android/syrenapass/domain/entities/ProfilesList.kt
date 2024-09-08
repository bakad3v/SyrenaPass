package com.android.syrenapass.domain.entities

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ProfilesList(
    @Serializable(with = ProfileDomainListSerializer::class)
    val list: PersistentList<ProfileDomain> = persistentListOf()
) {
    fun delete(id: Int): ProfilesList {
        val newList = list.removeAt(list.indexOfFirst { it.id == id })
        return ProfilesList(newList)
    }

    fun deleteMultiple(ids: Collection<Int>): ProfilesList {
        val newList = list.mutate { mutableList -> mutableList.filter { it.id !in ids } }
        return ProfilesList(newList)
    }

    fun add(profile: ProfileDomain): ProfilesList {
        val newList = list.add(profile)
        return ProfilesList(newList)
    }

    fun addMultiple(profiles: Collection<ProfileDomain>): ProfilesList {
        val newList = list.addAll(profiles)
        return ProfilesList(newList)
    }

    fun setDeletionStatus(id: Int,status: Boolean): ProfilesList {
        val newList = list.mutate { list ->
            val index = list.indexOfFirst { it.id == id }
            val newElement = list[index].copy(toDelete = status)
            list[index] = newElement
        }
        return ProfilesList(newList)
    }
}

@OptIn(ExperimentalSerializationApi::class)
class ProfileDomainListSerializer(
    private val serializer: KSerializer<ProfileDomain>,
) : KSerializer<PersistentList<ProfileDomain>> {

    private class PersistentListDescriptor :
        SerialDescriptor by serialDescriptor<List<ProfileDomain>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "kotlinx.serialization.immutable.persistentList"
    }

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: PersistentList<ProfileDomain>) {
        return ListSerializer(serializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<ProfileDomain> {
        return ListSerializer(serializer).deserialize(decoder).toPersistentList()
    }

}