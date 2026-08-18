package com.netraze.app.di

import com.netraze.app.data.remote.AuthInterceptor
import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.repository.AuthRepositoryImpl
import com.netraze.app.data.security.SecureSessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 127.0.0.1 connects to PC local server via adb reverse tcp:8000 tcp:8000 on physical USB device
    private const val BASE_URL = "http://127.0.0.1:8000/"

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionStore: SecureSessionStore): AuthInterceptor {
        return AuthInterceptor(sessionStore)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        sessionStore: SecureSessionStore
    ): AuthRepository {
        return AuthRepositoryImpl(authApi, sessionStore)
    }
}
