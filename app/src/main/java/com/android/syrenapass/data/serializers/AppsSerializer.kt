package com.android.syrenapass.data.serializers

import androidx.datastore.core.Serializer
import com.android.syrenapass.data.encryption.EncryptionAlias
import com.android.syrenapass.data.encryption.EncryptionManager
import com.android.syrenapass.data.entities.AppList
import com.android.syrenapass.data.entities.FilesList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AppsSerializer @Inject constructor(private val encryptionManager: EncryptionManager):
    Serializer<AppList> {

    override val defaultValue: AppList
        get() = AppList()


    override suspend fun readFrom(input: InputStream): AppList {
        val decryptedBytes = encryptionManager.decrypt(EncryptionAlias.DATASTORE.name,input)
        return try {
            Json.decodeFromString(deserializer = AppList.serializer(),
                string = decryptedBytes.decodeToString())
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AppList, output: OutputStream) {
        withContext(Dispatchers.IO) {
            encryptionManager.encrypt(
                EncryptionAlias.DATASTORE.name,
                Json.encodeToString(
                    serializer = AppList.serializer(),
                    value = t
                ).encodeToByteArray(),
                output
            )
        }
    }


}