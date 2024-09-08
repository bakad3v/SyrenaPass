package com.android.syrenapass.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface DatabaseEncryptionManager {
    suspend fun decryptCPS()
    suspend fun decryptDPS()
    suspend fun encryptCPS()
    suspend fun encryptDPS()
    val unlockedFlow: StateFlow<Boolean>
    fun interruptLocking()
}