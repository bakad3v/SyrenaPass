package com.android.syrenapass.domain.usecases.usb

import com.android.syrenapass.domain.entities.UsbSettings
import com.android.syrenapass.domain.repositories.UsbSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsbSettingsUseCase @Inject constructor(private val repository: UsbSettingsRepository) {
    operator fun invoke(): Flow<UsbSettings> {
        return repository.usbSettings
    }
}