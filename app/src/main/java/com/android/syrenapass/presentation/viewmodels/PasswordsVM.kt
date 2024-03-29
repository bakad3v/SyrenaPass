package com.android.syrenapass.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.syrenapass.domain.usecases.logs.WriteToLogsUseCase
import com.android.syrenapass.domain.usecases.passwordManager.CheckPasswordUseCase
import com.android.syrenapass.domain.usecases.passwordManager.GetPasswordStatusUseCase
import com.android.syrenapass.domain.usecases.passwordManager.SetPasswordUseCase
import com.android.syrenapass.presentation.states.PasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordsVM @Inject constructor(
  private val checkPasswordUseCase: CheckPasswordUseCase,
  private val setPasswordUseCase: SetPasswordUseCase,
  private val getPasswordStatusUseCase: GetPasswordStatusUseCase,
  private val passwordState: MutableSharedFlow<PasswordState>,
  private val writeToLogsUseCase: WriteToLogsUseCase,
) : ViewModel() {
  val passwordStatus = getPasswordStatusUseCase().map {
    if (it) {
      PasswordState.GetPassword
    } else {
      PasswordState.CreatePassword
    }
  }.mergeWith(passwordState).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PasswordState.GetPassword)

  fun passwordEntered(password: CharArray) {
    viewModelScope.launch {
      if (getPasswordStatusUseCase().first()) {
        checkPassword(password)
      } else {
        setPassword(password)
      }
    }
  }

  private fun <T> Flow<T>.mergeWith(another: Flow<T>): Flow<T> {
    return merge(this, another)
  }


  private suspend fun setPassword(password: CharArray) {
    setPasswordUseCase(password)
    passwordState.emit(PasswordState.CheckPasswordResults(true))
  }

  private suspend fun checkPassword(password: CharArray) {
    passwordState.emit(PasswordState.CheckPasswordResults(checkPasswordUseCase(password)))
  }

  suspend fun writeToLogs(string: String) {
      writeToLogsUseCase(string)
  }
}
