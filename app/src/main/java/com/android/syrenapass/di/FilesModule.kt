package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.FilesRepositoryImpl
import com.android.syrenapass.domain.entities.FilesSortOrder
import com.android.syrenapass.domain.repositories.FilesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FilesModule {
  @Binds
  @Singleton
  abstract fun bindFilesRepository(filesRepositoryImpl: FilesRepositoryImpl): FilesRepository

  companion object {

    @Provides
    @Singleton
    fun provideFilesSortOrderFlow(): MutableStateFlow<FilesSortOrder> =
      MutableStateFlow(FilesSortOrder.NAME_ASC)
  }
}
