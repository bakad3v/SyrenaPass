package com.android.aftools.presentation.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.aftools.R
import com.android.aftools.domain.entities.BruteforceSettings
import com.android.aftools.domain.entities.Permissions
import com.android.aftools.domain.entities.Settings
import com.android.aftools.domain.entities.Theme
import com.android.aftools.domain.entities.UsbSettings
import com.android.aftools.domain.usecases.bruteforce.GetBruteforceSettingsUseCase
import com.android.aftools.domain.usecases.bruteforce.SetBruteForceLimitUseCase
import com.android.aftools.domain.usecases.bruteforce.SetBruteForceStatusUseCase
import com.android.aftools.domain.usecases.passwordManager.SetPasswordUseCase
import com.android.aftools.domain.usecases.permissions.GetPermissionsUseCase
import com.android.aftools.domain.usecases.permissions.SetOwnerActiveUseCase
import com.android.aftools.domain.usecases.permissions.SetRootActiveUseCase
import com.android.aftools.domain.usecases.settings.SetMultiuserUIUseCase
import com.android.aftools.domain.usecases.settings.GetSettingsUseCase
import com.android.aftools.domain.usecases.settings.GetUserLimitUseCase
import com.android.aftools.domain.usecases.settings.SetClearAndHideUseCase
import com.android.aftools.domain.usecases.settings.SetLogdOnBootUseCase
import com.android.aftools.domain.usecases.settings.SetLogdOnStartUseCase
import com.android.aftools.domain.usecases.settings.SetRemoveItselfUseCase
import com.android.aftools.domain.usecases.settings.SetRunOnDuressUseCase
import com.android.aftools.domain.usecases.settings.SetSafeBootUseCase
import com.android.aftools.domain.usecases.settings.SetThemeUseCase
import com.android.aftools.domain.usecases.settings.SetTrimUseCase
import com.android.aftools.domain.usecases.settings.SetUserLimitUseCase
import com.android.aftools.domain.usecases.settings.SetWipeUseCase
import com.android.aftools.domain.usecases.usb.GetUsbSettingsUseCase
import com.android.aftools.domain.usecases.usb.SetUsbStatusUseCase
import com.android.aftools.presentation.actions.DialogActions
import com.android.aftools.presentation.utils.UIText
import com.android.aftools.superuser.superuser.SuperUserException
import com.android.aftools.superuser.superuser.SuperUserManager
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
    private val setLogdOnBootUseCase: SetLogdOnBootUseCase,
    private val setLogdOnStartUseCase: SetLogdOnStartUseCase,
    private val setClearAndHideUseCase: SetClearAndHideUseCase,
    private val enableMultiuserUIUseCase: SetMultiuserUIUseCase,
    private val getUserLimitUseCase: GetUserLimitUseCase,
    private val setUserLimitUseCase: SetUserLimitUseCase,
    private val setSafeBootUseCase: SetSafeBootUseCase,
    private val setRunOnDuressUseCase: SetRunOnDuressUseCase,
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

    fun setUserLimit(limit: Int) {
        viewModelScope.launch {
            try {
                setUserLimitUseCase(limit)
            } catch (e: SuperUserException) {
                settingsActionChannel.send(
                    DialogActions.ShowInfoDialog(
                        title = UIText.StringResource(R.string.user_limit_not_changed),
                        message = e.messageForLogs
                    )
                )
            }
        }
    }

    private fun cantGetUserLimit(message: UIText.StringResource) {
        viewModelScope.launch {
            settingsActionChannel.send(
                DialogActions.ShowInfoDialog(
                    title = UIText.StringResource(R.string.cant_get_user_limit),
                    message = message
                )
            )
        }
    }

    fun showChangeUserLimitDialog() {
        viewModelScope.launch {
            val hint = try {
                 getUserLimitUseCase()
            } catch (e: SuperUserException) {
                cantGetUserLimit(e.messageForLogs)
                return@launch
            }
            if (hint == null) {
                cantGetUserLimit(UIText.StringResource(R.string.number_not_found))
            }
            settingsActionChannel.send(
                DialogActions.ShowInputDigitDialog(
                    title = UIText.StringResource(R.string.set_users_limit),
                    message = UIText.StringResource(R.string.set_users_limit_long),
                    hint = hint.toString(),
                    range = 1..1000,
                    requestKey = CHANGE_USER_LIMIT_DIALOG
                )
            )
        }
    }

    fun setClearAndHide(status: Boolean) {
        viewModelScope.launch {
            setClearAndHideUseCase(status)
        }
    }

    fun showClearAndHideDialog() {
        viewModelScope.launch {
            settingsActionChannel.send(
                DialogActions.ShowQuestionDialog(
                    title = UIText.StringResource(R.string.clear_and_hide),
                    message = UIText.StringResource(R.string.clear_and_hide_long),
                    CLEAR_AND_HIDE_DIALOG
                )
            )
        }
    }

    fun showRunOnDuressPasswordDialog() {
        viewModelScope.launch {
            settingsActionChannel.send(
                DialogActions.ShowQuestionDialog(
                    title = UIText.StringResource(R.string.run_on_password),
                    message = UIText.StringResource(R.string.run_on_password_long),
                    RUN_ON_PASSWORD_DIALOG
                )
            )
        }
    }

    fun setRunOnDuressPassword(status: Boolean) {
        viewModelScope.launch {
            setRunOnDuressUseCase(status)
        }
    }

    fun askDhizuku() {
        superUserManager.askDeviceOwnerRights(
            ::onDhizukuRightsApprove,
            ::onDhizukuRightsDeny,
            ::onDhizukuAbsent
        )
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

    fun disableSafeBoot() {
        viewModelScope.launch {
            try {
                setSafeBootUseCase()
                settingsActionChannel.send(
                    DialogActions.ShowInfoDialog(
                        title = UIText.StringResource(R.string.safeboot_disabled),
                        message = UIText.StringResource(R.string.safeboot_disabled_long)
                    )
                )
            } catch (e: SuperUserException) {
                settingsActionChannel.send(
                    DialogActions.ShowInfoDialog(
                        title = UIText.StringResource(R.string.safeboot_not_disabled),
                        message = e.messageForLogs,
                    )
                )
            }
        }
    }

    fun enableMultiuserUI() {
        viewModelScope.launch {
            try {
                enableMultiuserUIUseCase()
                settingsActionChannel.send(
                    DialogActions.ShowQuestionDialog(
                        title = UIText.StringResource(R.string.multiuser_ui_unlocked),
                        message = UIText.StringResource(R.string.multiuser_ui_unlocked_long),
                        requestKey = OPEN_MULTIUSER_SETTINGS_DIALOG
                    )
                )
            } catch (e: SuperUserException) {
                settingsActionChannel.send(
                    DialogActions.ShowInfoDialog(
                        title = UIText.StringResource(R.string.multiuser_ui_unlock_failed),
                        message = e.messageForLogs,
                    )
                )
            }
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
                    range = 1..1000,
                    requestKey = MAX_PASSWORD_ATTEMPTS_DIALOG
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

    fun setLogdOnStartStatus(status: Boolean) {
        viewModelScope.launch {
            setLogdOnStartUseCase(status)
        }
    }

    fun setLogdOnBootStatus(status: Boolean) {
        viewModelScope.launch {
            setLogdOnBootUseCase(status)
        }
    }

    fun showLogdOnBootDialog() {
        viewModelScope.launch {
            settingsActionChannel.send(
                DialogActions.ShowQuestionDialog(
                    title = UIText.StringResource(R.string.logd_on_boot),
                    message = UIText.StringResource(R.string.logd_on_boot_long),
                    LOGD_ON_BOOT_DIALOG
                )
            )
        }
    }

    fun showLogdOnStartDialog() {
        viewModelScope.launch {
            settingsActionChannel.send(
                DialogActions.ShowQuestionDialog(
                    title = UIText.StringResource(R.string.logd_on_start),
                    message = UIText.StringResource(R.string.logd_on_start_long),
                    LOGD_ON_START_DIALOG
                )
            )
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
        const val LOGD_ON_BOOT_DIALOG = "logd_on_boot"
        const val LOGD_ON_START_DIALOG = "logd_on_start"
        const val CLEAR_AND_HIDE_DIALOG = "clear_and_hide"
        const val CHANGE_USER_LIMIT_DIALOG = "change_user_limit"
        const val MAX_PASSWORD_ATTEMPTS_DIALOG = "max_password_attempts"
        const val OPEN_MULTIUSER_SETTINGS_DIALOG = "open_multiuser_settings_dialog"
        const val RUN_ON_PASSWORD_DIALOG = "run_on_password_dialog"
    }

}
