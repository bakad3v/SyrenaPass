package com.android.syrenapass.domain.repositories

import com.android.syrenapass.domain.entities.UsbSettings
import kotlinx.coroutines.flow.Flow

interface UsbSettingsRepository {
    val usbSettings: Flow<UsbSettings>
    suspend fun setUsbConnectionStatus(status: Boolean)
}