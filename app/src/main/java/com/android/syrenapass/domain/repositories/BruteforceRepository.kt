package com.android.syrenapass.domain.repositories

import com.android.syrenapass.domain.entities.BruteforceSettings
import kotlinx.coroutines.flow.Flow

interface BruteforceRepository {
    val bruteforceSettings: Flow<BruteforceSettings>
    suspend fun setBruteforceStatus(status: Boolean)
    suspend fun setBruteforceLimit(limit: Int)
    suspend fun onWrongPassword(): Boolean
    suspend fun onRightPassword()
}