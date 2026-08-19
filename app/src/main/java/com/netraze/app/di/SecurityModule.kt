package com.netraze.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.netraze.app.data.security.AndroidKeystoreSessionCrypto
import com.netraze.app.data.security.SecureSessionStore
import com.netraze.app.data.security.SessionCrypto
import com.netraze.app.data.security.sessionDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityModule {

    @Binds
    @Singleton
    abstract fun bindSessionCrypto(
        impl: AndroidKeystoreSessionCrypto
    ): SessionCrypto

    companion object {
        @Provides
        @Singleton
        fun provideSessionDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return context.sessionDataStore
        }

        @Provides
        @Singleton
        fun provideSecureSessionStore(
            dataStore: DataStore<Preferences>,
            sessionCrypto: SessionCrypto
        ): SecureSessionStore {
            return SecureSessionStore(dataStore, sessionCrypto)
        }
    }
}
