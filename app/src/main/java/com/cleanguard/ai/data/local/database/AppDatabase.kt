package com.cleanguard.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cleanguard.ai.data.local.dao.*
import com.cleanguard.ai.data.local.entities.*

@Database(
    entities = [
        AppInfoEntity::class,
        ScanResultEntity::class,
        NotificationStatsEntity::class,
        KnownAdwareEntity::class,
        KnownScamAppEntity::class,
        KnownFakeCleanerEntity::class,
        KnownFakeAntivirusEntity::class,
        KnownBrowserHijackerEntity::class,
        KnownNotificationAbuserEntity::class,
        ExodusTrackerEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
    abstract fun scanResultDao(): ScanResultDao
    abstract fun notificationStatsDao(): NotificationStatsDao
    abstract fun threatIntelDao(): ThreatIntelDao
}
