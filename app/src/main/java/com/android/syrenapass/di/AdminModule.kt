package com.android.syrenapass.di

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.android.syrenapass.data.repositories.DeviceAdminImpl
import com.android.syrenapass.data.repositories.FilesRepositoryImpl
import com.android.syrenapass.domain.repositories.DeviceAdmin
import com.android.syrenapass.domain.repositories.FilesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminModule {
  @Binds
  @Singleton
  abstract fun bindAdminRepository(deviceAdminImpl: DeviceAdminImpl): DeviceAdmin

  companion object {
    @Provides
    @Singleton
    fun provideDevicePolicyManager(@ApplicationContext context: Context): DevicePolicyManager {
      return context.getSystemService(DevicePolicyManager::class.java)
    }
  }
}
