package com.netraze.app.di

import android.content.Context
import com.netraze.app.data.security.CryptoManager
import com.netraze.app.data.security.SecureSessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager = CryptoManager()

    @Provides
    @Singleton
    fun provideSecureSessionStore(
        @ApplicationContext context: Context,
        cryptoManager: CryptoManager
    ): SecureSessionStore {
        return SecureSessionStore(context, cryptoManager)
    }
}
