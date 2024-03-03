package com.android.syrenapass.presentation.validators

import com.android.syrenapass.R

/**
 * Simplified validator of user's given password
 */
class SimplePasswordValidator(private val password: String): BaseValidator() {
  override fun validate(): ValidateResult {
    if (password.length < 8) {
      return ValidateResult(false, R.string.short_password)
    }
    return ValidateResult(true)
  }
}
