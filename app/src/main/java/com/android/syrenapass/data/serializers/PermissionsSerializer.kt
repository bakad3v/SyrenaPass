package com.android.syrenapass.data.serializers

import androidx.datastore.core.Serializer
import com.android.syrenapass.data.encryption.EncryptionAlias
import com.android.syrenapass.data.encryption.EncryptionManager
import com.android.syrenapass.data.entities.LogList
import com.android.syrenapass.domain.entities.Permissions
import com.android.syrenapass.domain.entities.ProfilesList
import com.android.syrenapass.domain.entities.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class PermissionsSerializer @Inject constructor(private val encryptionManager: EncryptionManager):
    Serializer<Permissions> {
    override val defaultValue: Permissions
        get() = Permissions()

    override suspend fun readFrom(input: InputStream): Permissions {
        val decryptedBytes = encryptionManager.decrypt(EncryptionAlias.DATASTORE.name,input)
        return try {
            Json.decodeFromString(deserializer = Permissions.serializer(),
                string = decryptedBytes.decodeToString())
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: Permissions, output: OutputStream) {
        withContext(Dispatchers.IO) {
            encryptionManager.encrypt(
                EncryptionAlias.DATASTORE.name,
                Json.encodeToString(
                    serializer = Permissions.serializer(),
                    value = t
                ).encodeToByteArray(),
                output
            )
        }
    }

}