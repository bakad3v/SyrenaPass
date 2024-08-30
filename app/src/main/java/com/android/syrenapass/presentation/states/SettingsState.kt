package com.android.syrenapass.presentation.states

import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme

data class SettingsState(val active: Boolean=false, val isAdmin: Boolean=false, val isRooted: Boolean=false, val serviceWorking: Boolean=false, val theme: Theme= Theme.SYSTEM_THEME) {
  constructor(settings: Settings,isAdmin: Boolean,isRooted: Boolean) : this(settings.active,isAdmin, isRooted, settings.serviceWorking, settings.theme)
}
