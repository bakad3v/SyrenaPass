package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.SettingsRepositoryImpl
import com.android.syrenapass.domain.repositories.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
  @Binds
  @Singleton
  abstract fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository
}
