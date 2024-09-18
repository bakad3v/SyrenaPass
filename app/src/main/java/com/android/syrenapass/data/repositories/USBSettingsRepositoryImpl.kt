package com.android.syrenapass.data.repositories

import android.content.Context
import com.android.syrenapass.data.serializers.UsbSettingsSerializer
import com.android.syrenapass.datastoreDBA.dataStoreDirectBootAware
import com.android.syrenapass.domain.entities.UsbSettings
import com.android.syrenapass.domain.repositories.UsbSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class USBSettingsRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context, usbSettingsSerializer: UsbSettingsSerializer):
    UsbSettingsRepository {
    private val Context.usbDataStore by dataStoreDirectBootAware(
        DATASTORE_NAME,
        usbSettingsSerializer
    )

    companion object {
        private const val DATASTORE_NAME = "usb_datastore.json"
    }

    override val usbSettings: Flow<UsbSettings> = context.usbDataStore.data

    override suspend fun setUsbConnectionStatus(status: Boolean) {
        context.usbDataStore.updateData {
            it.copy(runOnConnection = status)
        }
    }
}