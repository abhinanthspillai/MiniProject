package com.netraze.app.di

import android.content.Context
import com.netraze.app.data.local.dao.HierarchyDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.remote.api.AuthApi
import com.netraze.app.data.remote.api.HierarchyApi
import com.netraze.app.data.remote.api.SurveyApi
import com.netraze.app.data.repository.AuthRepository
import com.netraze.app.data.repository.AuthRepositoryImpl
import com.netraze.app.data.repository.HierarchyRepository
import com.netraze.app.data.repository.HierarchyRepositoryImpl
import com.netraze.app.data.repository.SurveyRepository
import com.netraze.app.data.repository.SurveyRepositoryImpl
import com.netraze.app.data.security.AndroidKeystoreSessionCrypto
import com.netraze.app.data.security.SecureSessionStore
import com.netraze.app.data.security.sessionDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
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
    fun provideSecureSessionStore(@ApplicationContext context: Context): SecureSessionStore {
        val crypto = AndroidKeystoreSessionCrypto()
        return SecureSessionStore(context.sessionDataStore, crypto)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(sessionStore: SecureSessionStore): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val session = runBlocking { sessionStore.getSession() }
                session?.accessToken?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
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

    @Provides
    @Singleton
    fun provideHierarchyApi(retrofit: Retrofit): HierarchyApi {
        return retrofit.create(HierarchyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHierarchyRepository(
        hierarchyApi: HierarchyApi,
        hierarchyDao: HierarchyDao
    ): HierarchyRepository {
        return HierarchyRepositoryImpl(hierarchyApi, hierarchyDao)
    }

    @Provides
    @Singleton
    fun provideSurveyApi(retrofit: Retrofit): SurveyApi {
        return retrofit.create(SurveyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSurveyRepository(
        surveyApi: SurveyApi,
        surveyDao: SurveyDao,
        sessionStore: SecureSessionStore
    ): SurveyRepository {
        return SurveyRepositoryImpl(surveyApi, surveyDao, sessionStore)
    }
}
