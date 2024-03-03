package com.android.syrenapass.presentation.states

sealed class PasswordState {
  data object CreatePassword: PasswordState()
  data object GetPassword: PasswordState()
  data class CheckPasswordResults(val rightPassword: Boolean): PasswordState()
}
