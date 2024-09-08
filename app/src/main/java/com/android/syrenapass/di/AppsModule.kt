package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.AppsRepositoryImpl
import com.android.syrenapass.domain.repositories.AppsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppsModule {
    @Binds
    @Singleton
    abstract fun bindAppsRepository(appsRepositoryImpl: AppsRepositoryImpl): AppsRepository
}