package com.android.syrenapass.data.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject

class EncryptionManagerImpl @Inject constructor(@ApplicationContext private val context: Context): EncryptionManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun getEncryptCipher(alias: String) = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey(alias))
    }

    private fun getDecryptCipherForIv(alias: String,iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(alias), IvParameterSpec(iv))
        }
    }

    private fun getKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey(alias)
    }

    private fun createKey(alias: String): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }


    override fun getSalt(): ByteArray {
        val array = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(array)
        return array
    }


    override fun encrypt(alias: String,bytes: ByteArray, outputStream: OutputStream): ByteArray {
        val encryptCypher = getEncryptCipher(alias)
        val encryptedBytes = encryptCypher.doFinal(bytes)
        outputStream.use {
            it.write(encryptCypher.iv.size)
            it.write(encryptCypher.iv)
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }

    override fun getDatabaseKey(fileName: String): ByteArray{
        val file = File(context.filesDir, fileName)
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS).
                setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return if(file.exists()){
            encryptedFile.openFileInput().use { it.readBytes() }
        } else {
            generateDatabaseKey().also {
                    passPhrase ->
                encryptedFile.openFileOutput().use { it.write(passPhrase) }
            }
        }
    }

    private fun generateDatabaseKey(): ByteArray{
        val random = SecureRandom.getInstanceStrong()
        val result = ByteArray(32)
        random.nextBytes(result)
        return result
    }


    override fun decrypt(alias: String,inputStream: InputStream): ByteArray {
        return inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)
            val encryptedBytes = it.readBytes()
            getDecryptCipherForIv(alias,iv).doFinal(encryptedBytes)
        }
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val SALT_LENGTH = 16
    }

}