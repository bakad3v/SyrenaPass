package com.android.syrenapass.data.serializers

import androidx.datastore.core.Serializer
import com.android.syrenapass.data.encryption.EncryptionAlias
import com.android.syrenapass.data.encryption.EncryptionManager
import com.android.syrenapass.data.entities.LogList
import com.android.syrenapass.domain.entities.ProfilesList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class LogsSerializer @Inject constructor(private val encryptionManager: EncryptionManager):
    Serializer<LogList> {

    override val defaultValue: LogList
        get() = LogList()


    override suspend fun readFrom(input: InputStream): LogList {
        val decryptedBytes = encryptionManager.decrypt(EncryptionAlias.DATASTORE.name,input)
        return try {
            Json.decodeFromString(deserializer = LogList.serializer(),
                string = decryptedBytes.decodeToString())
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: LogList, output: OutputStream) {
        withContext(Dispatchers.IO) {
            encryptionManager.encrypt(
                EncryptionAlias.DATASTORE.name,
                Json.encodeToString(
                    serializer = LogList.serializer(),
                    value = t
                ).encodeToByteArray(),
                output
            )
        }
    }


}