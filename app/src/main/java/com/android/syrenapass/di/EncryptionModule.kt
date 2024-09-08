package com.android.syrenapass.di

import com.android.syrenapass.data.encryption.EncryptionManager
import com.android.syrenapass.data.encryption.EncryptionManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EncryptionModule {


    @Binds
    @Singleton
    abstract fun provideEncryptionManager(encryptionManagerImpl: EncryptionManagerImpl): EncryptionManager
}