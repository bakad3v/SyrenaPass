package com.android.syrenapass.presentation.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.syrenapass.R
import com.android.syrenapass.domain.entities.BruteforceSettings
import com.android.syrenapass.domain.entities.Permissions
import com.android.syrenapass.domain.entities.Settings
import com.android.syrenapass.domain.entities.Theme
import com.android.syrenapass.domain.entities.UsbSettings
import com.android.syrenapass.domain.usecases.bruteforce.GetBruteforceSettingsUseCase
import com.android.syrenapass.domain.usecases.bruteforce.SetBruteForceLimitUseCase
import com.android.syrenapass.domain.usecases.bruteforce.SetBruteForceStatusUseCase
import com.android.syrenapass.domain.usecases.passwordManager.SetPasswordUseCase
import com.android.syrenapass.domain.usecases.permissions.GetPermissionsUseCase
import com.android.syrenapass.domain.usecases.permissions.SetOwnerActiveUseCase
import com.android.syrenapass.domain.usecases.permissions.SetRootActiveUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.domain.usecases.settings.SetRemoveItselfUseCase
import com.android.syrenapass.domain.usecases.settings.SetThemeUseCase
import com.android.syrenapass.domain.usecases.settings.SetTrimUseCase
import com.android.syrenapass.domain.usecases.settings.SetWipeUseCase
import com.android.syrenapass.domain.usecases.usb.GetUsbSettingsUseCase
import com.android.syrenapass.domain.usecases.usb.SetUsbStatusUseCase
import com.android.syrenapass.presentation.actions.DialogActions
import com.android.syrenapass.presentation.utils.UIText
import com.android.syrenapass.superuser.superuser.SuperUserManager
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
  private val setPasswordUseCase: SetPasswordUseCase,
  private val settingsActionChannel: Channel<DialogActions>,
  private val setRemoveItselfUseCase: SetRemoveItselfUseCase,
  private val setTrimUseCase: SetTrimUseCase,
  private val setWipeUseCase: SetWipeUseCase,
  private val setOwnerActiveUseCase: SetOwnerActiveUseCase,
  private val setRootActiveUseCase: SetRootActiveUseCase,
  private val setUsbStatusUseCase: SetUsbStatusUseCase,
  private val setBruteForceStatusUseCase: SetBruteForceStatusUseCase,
  private val setBruteForceLimitUseCase: SetBruteForceLimitUseCase,
  private val superUserManager: SuperUserManager,
  getPermissionsUseCase: GetPermissionsUseCase,
  getUSBSettingsUseCase: GetUsbSettingsUseCase,
  getBruteforceSettingsUseCase: GetBruteforceSettingsUseCase
) : ViewModel() {

  val settingsActionsFlow = settingsActionChannel.receiveAsFlow()

  val settingsState = getSettingsUseCase().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    Settings()
  )

  val usbSettingState = getUSBSettingsUseCase().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    UsbSettings()
  )

  val permissionsState = getPermissionsUseCase().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    Permissions()
  )

  val bruteforceProtectionState = getBruteforceSettingsUseCase().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    BruteforceSettings()
  )

  fun adminRightsIntent(): Intent {
    return superUserManager.askDeviceAdminRights()
  }

  fun setBruteForceLimit(limit: Int) {
    viewModelScope.launch {
      setBruteForceLimitUseCase(limit)
    }
  }

  fun askDhizuku() {
    superUserManager.askDeviceOwnerRights(::onDhizukuRightsApprove, ::onDhizukuRightsDeny, ::onDhizukuAbsent)
  }

  private fun onDhizukuAbsent() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.install_dhizuku),
          message = UIText.StringResource(R.string.install_dhizuku_long),
          requestKey = INSTALL_DIZUKU_DIALOG
        )
      )
    }
  }

  private fun onDhizukuRightsApprove() {
    viewModelScope.launch {
      setOwnerActiveUseCase(true)
    }
  }

  private fun onDhizukuRightsDeny() {
    viewModelScope.launch {
      setOwnerActiveUseCase(false)
    }
  }

  fun askRoot() {
    viewModelScope.launch {
      val rootResult = superUserManager.askRootRights()
      setRootActiveUseCase(rootResult)
    }
  }

  fun setRunTRIM(status: Boolean) {
    viewModelScope.launch {
      setTrimUseCase(status)
    }
  }

  fun setWipe(status: Boolean) {
    viewModelScope.launch {
      setWipeUseCase(status)
    }
  }

  fun setRemoveItself(status: Boolean) {
    viewModelScope.launch {
      setRemoveItselfUseCase(status)
    }
  }

  fun setUsbConnectionStatus(status: Boolean) {
    viewModelScope.launch {
      setUsbStatusUseCase(status)
    }
  }

  fun setBruteforceProtection(status: Boolean) {
    viewModelScope.launch {
      setBruteForceStatusUseCase(status)
    }
  }



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
      setPasswordUseCase(password.toCharArray())
    }
  }

  fun showAccessibilityServiceDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.accessibility_service_title),
          message = UIText.StringResource(R.string.accessibility_service_long),
          MOVE_TO_ACCESSIBILITY_SERVICE
        )
      )
    }
  }

  fun showTRIMDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.run_trim),
          message = UIText.StringResource(R.string.trim_long),
          TRIM_DIALOG
        )
      )
    }
  }

  fun showWipeDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.wipe_data),
          message = UIText.StringResource(R.string.wipe_long),
          WIPE_DIALOG
        )
      )
    }
  }

  fun showSelfDestructionDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.remove_itself),
          message = UIText.StringResource(R.string.self_destruct_long),
          SELF_DESTRUCTION_DIALOG
        )
      )
    }
  }

  fun showRunOnUSBConnectionDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.run_on_connection),
          message = UIText.StringResource(R.string.run_on_usb_connection_long),
          USB_DIALOG
        )
      )
    }
  }

  fun showBruteforceDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowQuestionDialog(
          title = UIText.StringResource(R.string.bruteforce_defense),
          message = UIText.StringResource(R.string.bruteforce_defence_long),
          BRUTEFORCE_DIALOG
        )
      )
    }
  }

  fun editMaxPasswordAttemptsDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowInputDigitDialog(
          title = UIText.StringResource(R.string.allowed_attempts),
          message = UIText.StringResource(R.string.password_attempts_number),
          hint = bruteforceProtectionState.value.allowedAttempts.toString(),
          range = 1..1000
        )
      )
    }
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

  fun disableAdmin() {
    viewModelScope.launch {
      superUserManager.removeAdminRights()
    }
  }

  fun showRootDisableDialog() {
    viewModelScope.launch {
      settingsActionChannel.send(
        DialogActions.ShowInfoDialog(
          message = UIText.StringResource(R.string.disable_root_long),
          title = UIText.StringResource(R.string.disable_root)
        )
      )
      setRootActiveUseCase(false)
    }
  }


  companion object {
    const val MOVE_TO_ACCESSIBILITY_SERVICE = "move_to_accessibility_service"
    const val MOVE_TO_ADMIN_SETTINGS = "move_to_admin_settings"
    const val TRIM_DIALOG = "trim_dialog"
    const val WIPE_DIALOG = "wipe_dialog"
    const val SELF_DESTRUCTION_DIALOG = "selfdestruct_dialog"
    const val USB_DIALOG = "usb_dialog"
    const val BRUTEFORCE_DIALOG = "bruteforce_dialog"
    const val INSTALL_DIZUKU_DIALOG = "install_dizuku"
  }

}
