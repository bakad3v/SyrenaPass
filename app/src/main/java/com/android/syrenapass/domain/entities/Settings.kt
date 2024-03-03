package com.android.syrenapass.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Settings(val active: Boolean=false, val theme: Theme=Theme.SYSTEM_THEME)
