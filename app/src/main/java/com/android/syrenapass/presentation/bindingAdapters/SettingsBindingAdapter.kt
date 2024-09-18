package com.android.syrenapass.presentation.bindingAdapters

import androidx.databinding.BindingAdapter
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

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

