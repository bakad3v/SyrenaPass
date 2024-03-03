package com.android.syrenapass.di

import com.android.syrenapass.presentation.actions.DialogActions
import com.android.syrenapass.presentation.actions.FileSettingsAction
import com.android.syrenapass.presentation.actions.LogsActions
import com.android.syrenapass.presentation.states.ActivityState
import com.android.syrenapass.presentation.states.LogsDataState
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@InstallIn(ViewModelComponent::class)
@Module
class ViewModelsModule {

  @Provides
  fun provideDeletionSettingsActionChannel(): Channel<FileSettingsAction> = Channel()

  @Provides
  fun provideLogsStateFlow(): MutableSharedFlow<LogsDataState> = MutableSharedFlow()

  @Provides
  fun provideLogsActionChannel(): Channel<LogsActions> = Channel()

  @Provides
  fun provideActivityStateFlow(): MutableStateFlow<ActivityState> = MutableStateFlow(ActivityState.PasswordActivityState)

  @Provides
  fun provideSettingsActionsChannel(): Channel<DialogActions> = Channel()
}
