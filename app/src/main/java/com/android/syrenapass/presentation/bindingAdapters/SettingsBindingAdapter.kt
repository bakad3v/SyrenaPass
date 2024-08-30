package com.android.syrenapass.presentation.bindingAdapters

import androidx.databinding.BindingAdapter
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.presentation.states.SettingsState
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

@BindingAdapter("buttonActivationStatus")
fun MaterialButton.buttonActivationStatus(state: Settings) {
  if  (state.active) {
    setText(R.string.disable_files_deletion)
  } else {
    setText(R.string.enable_files_deletion)
  }
}

@BindingAdapter("currentTheme")
fun MaterialTextView.themeText(state: Settings) {
  text = when(state.theme) {
    Theme.SYSTEM_THEME -> context.getString(R.string.system_theme)
    Theme.DARK_THEME -> context.getString(R.string.dark_theme)
    Theme.LIGHT_THEME -> context.getString(R.string.light_theme)
  }
}

@BindingAdapter("runningService")
fun MaterialButton.isServiceRunning(state: Settings) {
  text = if (state.serviceWorking) {
    context.getString(R.string.stop_accessibility_service)
  } else {
    context.getString(R.string.start_accessibility_service)
  }
}

@BindingAdapter("adminActive")
fun MaterialButton.isAdminActive(state: Settings) {
  text = if (state.isAdmin) {
    context.getString(R.string.revoke_admin_rights)
  } else {
    context.getString(R.string.grant_admin_rights)
  }
}

