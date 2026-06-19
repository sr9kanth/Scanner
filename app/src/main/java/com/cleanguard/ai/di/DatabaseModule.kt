package com.cleanguard.ai.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cleanguard.ai.data.local.dao.*
import com.cleanguard.ai.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cleanguard.db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        seedThreatIntel(db)
                    }
                }
            })
            .build()

    private fun seedThreatIntel(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        // known_adware: packageName, displayName, reason, addedAt
        db.execSQL(
            "INSERT OR REPLACE INTO known_adware (packageName, displayName, reason, addedAt) VALUES " +
                "('com.notification.lucky.rewards', 'Lucky Rewards', 'Known adware', $now)," +
                "('com.battery.saver.boost', 'Battery Saver Boost', 'Known adware', $now)"
        )

        // known_scam_apps: packageName, displayName, scamType, reason, addedAt
        db.execSQL(
            "INSERT OR REPLACE INTO known_scam_apps (packageName, displayName, scamType, reason, addedAt) VALUES " +
                "('com.scam.giftcards.win', 'Gift Cards Win', 'gift_card', 'Known scam app', $now)," +
                "('org.scam.prizes.instant', 'Instant Prizes', 'prize', 'Known scam app', $now)"
        )

        // known_fake_cleaners: packageName, displayName, reason, addedAt
        db.execSQL(
            "INSERT OR REPLACE INTO known_fake_cleaners (packageName, displayName, reason, addedAt) VALUES " +
                "('com.cleaner.speedup.security', 'Cleaner SpeedUp Security', 'Known fake cleaner', $now)," +
                "('com.super.clean.cool', 'Super Clean Cool', 'Known fake cleaner', $now)"
        )

        // known_fake_antivirus: packageName, displayName, reason, addedAt
        db.execSQL(
            "INSERT OR REPLACE INTO known_fake_antivirus (packageName, displayName, reason, addedAt) VALUES " +
                "('com.fake.antivirus.extreme', 'Antivirus Extreme', 'Known fake antivirus', $now)"
        )

        // known_browser_hijackers: packageName, displayName, reason, addedAt
        db.execSQL(
            "INSERT OR REPLACE INTO known_browser_hijackers (packageName, displayName, reason, addedAt) VALUES " +
                "('com.browser.searchescape', 'Search Escape', 'Known browser hijacker', $now)"
        )

        // known_notification_abusers: packageName, displayName, reason, addedAt
        db.execSQL(
            "INSERT OR REPLACE INTO known_notification_abusers (packageName, displayName, reason, addedAt) VALUES " +
                "('com.news.popup.daily', 'News Popup Daily', 'Known notification abuser', $now)"
        )

        // exodus_trackers: packageName, appName, trackerCount
        db.execSQL(
            "INSERT OR REPLACE INTO exodus_trackers (packageName, appName, trackerCount) VALUES " +
                "('com.facebook.katana', 'Facebook', 7)," +
                "('com.whatsapp', 'WhatsApp', 1)," +
                "('com.instagram.android', 'Instagram', 5)"
        )
    }

    @Provides fun provideAppInfoDao(db: AppDatabase): AppInfoDao = db.appInfoDao()
    @Provides fun provideScanResultDao(db: AppDatabase): ScanResultDao = db.scanResultDao()
    @Provides fun provideNotificationStatsDao(db: AppDatabase): NotificationStatsDao = db.notificationStatsDao()
    @Provides fun provideThreatIntelDao(db: AppDatabase): ThreatIntelDao = db.threatIntelDao()
}
