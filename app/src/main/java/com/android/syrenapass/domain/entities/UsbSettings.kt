package com.android.syrenapass.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class UsbSettings(val runOnConnection: Boolean=false)