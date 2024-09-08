package com.android.syrenapass.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class Settings(val active: Boolean=false, val isAdmin: Boolean=false, val isOwner: Boolean=false, val isRoot: Boolean=false, val serviceWorking: Boolean = false, val deletionActivated: Boolean=false, val theme: Theme=Theme.SYSTEM_THEME, val runOnBootStatus: RunOnBootStatus = RunOnBootStatus.NOT_CHECKED, val trim: Boolean=false, val wipe: Boolean=false, val runRoot: Boolean=false, val sendBroadcast: Boolean=false, val removeItself: Boolean=false, val usbDisabled: Boolean=false)