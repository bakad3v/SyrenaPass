package com.android.syrenapass.data.serializers

import androidx.datastore.core.Serializer
import com.android.syrenapass.data.encryption.EncryptionAlias
import com.android.syrenapass.data.encryption.EncryptionManager
import com.android.syrenapass.domain.entities.ProfileDomain
import com.android.syrenapass.domain.entities.ProfileDomainListSerializer
import com.android.syrenapass.domain.entities.ProfilesList
import com.android.syrenapass.domain.entities.Settings
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class ProfilesSerializer @Inject constructor(private val encryptionManager: EncryptionManager):
    Serializer<ProfilesList> {

    override val defaultValue: ProfilesList
        get() = ProfilesList()


    override suspend fun readFrom(input: InputStream): ProfilesList{
        val decryptedBytes = encryptionManager.decrypt(EncryptionAlias.DATASTORE.name,input)
        return try {
            Json.decodeFromString(deserializer = ProfilesList.serializer(),
                string = decryptedBytes.decodeToString())
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: ProfilesList, output: OutputStream) {
        withContext(Dispatchers.IO) {
            encryptionManager.encrypt(
                EncryptionAlias.DATASTORE.name,
                Json.encodeToString(
                    serializer = ProfilesList.serializer(),
                    value = t
                ).encodeToByteArray(),
                output
            )
        }
    }


}