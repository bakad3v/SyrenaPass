package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.ProfilesRepositoryImpl
import com.android.syrenapass.domain.repositories.ProfilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfilesModule {
    @Binds
    @Singleton
    abstract fun provideProfilesRepository(profilesRepositoryImpl: ProfilesRepositoryImpl): ProfilesRepository
}