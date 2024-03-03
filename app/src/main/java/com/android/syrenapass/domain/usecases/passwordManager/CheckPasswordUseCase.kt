package com.android.syrenapass.domain.usecases.passwordManager

import com.android.syrenapass.domain.repositories.PasswordManager
import javax.inject.Inject

class CheckPasswordUseCase @Inject constructor(private val repository: PasswordManager) {
  suspend operator fun invoke(password: CharArray): Boolean {
    return repository.checkPassword(password)
  }
}
