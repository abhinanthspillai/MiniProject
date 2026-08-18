package com.netraze.app.di

import android.content.Context
import androidx.room.Room
import com.netraze.app.data.local.NetrazeDatabase
import com.netraze.app.data.local.dao.HierarchyDao
import com.netraze.app.data.local.dao.ScanAttemptDao
import com.netraze.app.data.local.dao.ScanCycleDao
import com.netraze.app.data.local.dao.SpatialPositionDao
import com.netraze.app.data.local.dao.SurveyDao
import com.netraze.app.data.local.dao.WifiObservationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNetrazeDatabase(
        @ApplicationContext context: Context
    ): NetrazeDatabase {
        return Room.databaseBuilder(
            context,
            NetrazeDatabase::class.java,
            "netraze.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideHierarchyDao(database: NetrazeDatabase): HierarchyDao = database.hierarchyDao()

    @Provides
    fun provideSurveyDao(database: NetrazeDatabase): SurveyDao = database.surveyDao()

    @Provides
    fun provideSpatialPositionDao(database: NetrazeDatabase): SpatialPositionDao = database.spatialPositionDao()

    @Provides
    fun provideScanAttemptDao(database: NetrazeDatabase): ScanAttemptDao = database.scanAttemptDao()

    @Provides
    fun provideScanCycleDao(database: NetrazeDatabase): ScanCycleDao = database.scanCycleDao()

    @Provides
    fun provideWifiObservationDao(database: NetrazeDatabase): WifiObservationDao = database.wifiObservationDao()
}
