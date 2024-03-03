package com.android.syrenapass.di

import com.android.syrenapass.SyrenaApp
import com.android.syrenapass.TopLevelFunctions.getEpochDays
import com.android.syrenapass.data.db.LogDao
import com.android.syrenapass.data.repositories.LogsRepositoryImpl
import com.android.syrenapass.domain.repositories.LogsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LogsModule {
  @Binds
  @Singleton
  abstract fun bindLogsRepository(filesRepositoryImpl: LogsRepositoryImpl): LogsRepository

  companion object {
    @Provides
    @Singleton
    fun provideLogDao(): LogDao? =
      SyrenaApp.getDatabase()?.myLogDao()

    @Provides
    @Singleton
    fun provideDaysFlow(): MutableStateFlow<Long> {
      return MutableStateFlow(LocalDateTime.now().getEpochDays())
    }


  }
}
