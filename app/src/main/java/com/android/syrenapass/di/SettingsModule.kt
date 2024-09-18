package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.BruteforceSettingsRepositoryImpl
import com.android.syrenapass.data.repositories.PermissionsRepositoryImpl
import com.android.syrenapass.data.repositories.SettingsRepositoryImpl
import com.android.syrenapass.data.repositories.USBSettingsRepositoryImpl
import com.android.syrenapass.domain.repositories.BruteforceRepository
import com.android.syrenapass.domain.repositories.PermissionsRepository
import com.android.syrenapass.domain.repositories.SettingsRepository
import com.android.syrenapass.domain.repositories.UsbSettingsRepository
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

  @Binds
  @Singleton
  abstract fun providePermissionsRepository(permissionsRepositoryImpl: PermissionsRepositoryImpl): PermissionsRepository

  @Binds
  @Singleton
  abstract fun provideBruteforceRepository(bruteforceSettingsRepositoryImpl: BruteforceSettingsRepositoryImpl): BruteforceRepository

  @Binds
  @Singleton
  abstract fun provideUsbSettingsRepository(usbSettingsRepositoryImpl: USBSettingsRepositoryImpl): UsbSettingsRepository
}
