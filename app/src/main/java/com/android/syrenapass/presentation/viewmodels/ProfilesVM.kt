package com.android.syrenapass.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.syrenapass.R
import com.android.syrenapass.domain.usecases.profiles.GetProfilesUseCase
import com.android.syrenapass.domain.usecases.profiles.RefreshProfilesUseCase
import com.android.syrenapass.domain.usecases.profiles.SetProfileDeletionStatusUseCase
import com.android.syrenapass.domain.usecases.settings.GetSettingsUseCase
import com.android.syrenapass.domain.usecases.settings.SetDeleteProfilesUseCase
import com.android.syrenapass.presentation.actions.DialogActions
import com.android.syrenapass.presentation.states.LogsDataState
import com.android.syrenapass.presentation.states.ProfilesDataState
import com.android.syrenapass.presentation.utils.UIText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesVM @Inject constructor(
    getProfilesUseCase: GetProfilesUseCase,
    private val setProfilesDeletionStatusUseCase: SetProfileDeletionStatusUseCase,
    private val setDeleteProfilesUseCase: SetDeleteProfilesUseCase,
    private val refreshProfilesUseCase: RefreshProfilesUseCase,
    private val dialogActionsChannel: Channel<DialogActions>,
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    val profileActions = dialogActionsChannel.receiveAsFlow()

    val profiles = getProfilesUseCase().map { ProfilesDataState.ViewData(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfilesDataState.Loading
    )

    val profileDeletionEnabled = getSettingsUseCase().map { it.deleteProfiles }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun setProfileDeletionStatus(id: Int, status: Boolean) {
        viewModelScope.launch {
            setProfilesDeletionStatusUseCase(id, status)
        }
    }

    fun changeDeletionEnabled() {
        viewModelScope.launch {
            setDeleteProfilesUseCase(!profileDeletionEnabled.value)
        }
    }

    fun refreshProfilesData() {
        viewModelScope.launch {
            refreshProfilesUseCase()
        }
    }

    fun showFAQ() {
        viewModelScope.launch {
            dialogActionsChannel.send(
                DialogActions.ShowInfoDialog (
                    title = UIText.StringResource(R.string.profiles),
                    message = UIText.StringResource(R.string.profiles_faq)
                )
            )
        }
    }

    fun showChangeDeletionEnabledDialog() {
        viewModelScope.launch {
            if (profileDeletionEnabled.value) {
                changeDeletionEnabled()
                return@launch
            }
            dialogActionsChannel.send(
                DialogActions.ShowQuestionDialog (
                    title = UIText.StringResource(R.string.enable_profile_deletion),
                    message = UIText.StringResource(R.string.enable_profile_deletion_long),
                    requestKey = CHANGE_PROFILES_DELETION_ENABLED
                )
            )
        }
    }

    companion object {
        const val CHANGE_PROFILES_DELETION_ENABLED = "change_profiles_deletion_enabled"
    }
}