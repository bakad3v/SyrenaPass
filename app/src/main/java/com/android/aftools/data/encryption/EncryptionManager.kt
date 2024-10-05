package com.android.aftools.data.encryption

import java.io.InputStream
import java.io.OutputStream

interface EncryptionManager {
    fun encrypt(alias: String, bytes: ByteArray, outputStream: OutputStream): ByteArray
    fun decrypt(alias: String, inputStream: InputStream): ByteArray
}