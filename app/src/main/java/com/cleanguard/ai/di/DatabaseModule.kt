package com.cleanguard.ai.di

import android.content.Context
import androidx.room.Room
import com.cleanguard.ai.data.local.dao.*
import com.cleanguard.ai.data.local.database.AppDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cleanguard.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideAppInfoDao(db: AppDatabase): AppInfoDao = db.appInfoDao()
    @Provides fun provideScanResultDao(db: AppDatabase): ScanResultDao = db.scanResultDao()
    @Provides fun provideNotificationStatsDao(db: AppDatabase): NotificationStatsDao = db.notificationStatsDao()
    @Provides fun provideThreatIntelDao(db: AppDatabase): ThreatIntelDao = db.threatIntelDao()
}
