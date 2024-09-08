package com.android.syrenapass.data.encryption

import java.io.InputStream
import java.io.OutputStream

interface EncryptionManager {
    fun encrypt(alias: String, bytes: ByteArray, outputStream: OutputStream): ByteArray
    fun decrypt(alias: String, inputStream: InputStream): ByteArray
    fun getSalt(): ByteArray
    fun getDatabaseKey(fileName: String): ByteArray
}