package com.android.syrenapass.presentation.bindingAdapters

import androidx.databinding.BindingAdapter
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
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
fun MaterialTextView.buttonActivationStatus(state: Settings) {
  text = when(state.theme) {
    Theme.SYSTEM_THEME -> context.getString(R.string.system_theme)
    Theme.DARK_THEME -> context.getString(R.string.dark_theme)
    Theme.LIGHT_THEME -> context.getString(R.string.light_theme)
  }
}
