package com.android.syrenapass.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Settings(val active: Boolean=false, val isAdmin: Boolean = false, val serviceWorking: Boolean = false, val theme: Theme=Theme.SYSTEM_THEME)
