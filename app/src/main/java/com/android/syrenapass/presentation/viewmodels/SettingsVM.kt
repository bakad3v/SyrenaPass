package com.android.syrenapass.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.domain.usecases.passwordManager.ChangePasswordUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.domain.usecases.settings.SetActiveUseCase
import com.android.syrenapass.domain.usecases.settings.SetInactiveUseCase
import com.android.syrenapass.domain.usecases.settings.SetThemeUseCase
import com.android.syrenapass.presentation.actions.DialogActions
import com.android.syrenapass.presentation.utils.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVM @Inject constructor(
  getSettingsUseCase: GetSettingsUseCase,
  private val setThemeUseCase: SetThemeUseCase,
  private val changePasswordUseCase: ChangePasswordUseCase,
  private val settingsActionChannel: Channel<DialogActions>,
  private val setInactiveUseCase: SetInactiveUseCase,
  private val setActiveUseCase: SetActiveUseCase
) : ViewModel() {

  val settingsActionsFlow = settingsActionChannel.receiveAsFlow()

  val settingsState = getSettingsUseCase().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    Settings()
  )

  fun setTheme(theme: Theme) {
    viewModelScope.launch {
      setThemeUseCase(theme)
    }
  }

  fun showPasswordInput() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowInputPasswordDialog(
          title = UIText.StringResource(R.string.change_password),
          hint = "",
          message = UIText.StringResource(R.string.enter_password_long)
        )
      )
    }
  }

  fun setPassword(password: String) {
    viewModelScope.launch {
      changePasswordUseCase(password)
    }
  }

  fun changeDeletionState() {
    viewModelScope.launch {
      if (settingsState.value.active) {
        setInactiveUseCase()
      } else {
        showConfirmationDialog()
      }
    }
  }

  fun setActive() {
    viewModelScope.launch {
      setActiveUseCase()
    }
  }

  private suspend fun showConfirmationDialog() {
    settingsActionChannel.send(
      DialogActions.ShowQuestionDialog(
        title = UIText.StringResource(R.string.do_you_want_to_enable_deletion),
        message = UIText.StringResource(R.string.enable_deletion_long),
        CONFIRM_AUTODELETION_REQUEST
      )
    )
  }

  fun showFaq() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowInfoDialog(
          title = UIText.StringResource(R.string.about_settings),
          message = UIText.StringResource(R.string.settings_faq)
        )
      )
    }
  }

  companion object {
    const val CONFIRM_AUTODELETION_REQUEST = "confirm_autodeletion_start"
  }

}
