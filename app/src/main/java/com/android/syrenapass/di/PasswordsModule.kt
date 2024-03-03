package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.PasswordManagerImpl
import com.android.syrenapass.domain.repositories.PasswordManager
import com.android.syrenapass.presentation.states.PasswordState
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PasswordsModule {
  @Binds
  @Singleton
  abstract fun providePasswordsManager(passwordManagerImpl: PasswordManagerImpl) : PasswordManager

  companion object {
    @Provides
    @Singleton
    fun providesPasswordStateFlow(): MutableSharedFlow<PasswordState> {
      return MutableSharedFlow()
    }
  }
}
