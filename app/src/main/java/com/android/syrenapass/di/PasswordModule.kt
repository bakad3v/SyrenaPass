package com.android.syrenapass.di

import com.android.syrenapass.data.repositories.PasswordManagerImpl
import com.android.syrenapass.domain.repositories.PasswordManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PasswordModule {

    @Binds
    @Singleton
    abstract fun bindPasswordManager(passwordManagerImpl: PasswordManagerImpl): PasswordManager

    //I am temporarily using default values
    companion object {

        @Provides
        @Singleton
        fun provideArgon2(): Argon2 =
            Argon2.Builder(Version.LATEST)
                .type(Type.Argon2id)
                .memoryCost(MemoryCost.MiB(MEMORY_COST))
                .parallelism(PARALLELISM)
                .iterations(ITERATIONS)
                .build()

        private const val MEMORY_COST = 32
        private const val PARALLELISM = 1
        private const val ITERATIONS = 3
    }
}