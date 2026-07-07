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

                // fallbackToDestructiveMigration recreates tables without calling onCreate
                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        seedThreatIntel(db)
                    }
                }
            })
            .build()

    /**
     * Bootstrap threat-intel entries. Every package below is a real, publicly documented
     * malicious app that was removed from Google Play:
     *  - Joker premium-subscription fraud family (Check Point research, April 2020)
     *  - HiddenAds fake-cleaner adware campaign, 8M+ installs (McAfee Labs, 2022)
     *  - SharkBot banking-trojan droppers posing as antivirus (Check Point research, March 2022)
     */
    private fun seedThreatIntel(db: SupportSQLiteDatabase) {
        val now = System.currentTimeMillis()

        // known_adware / known_fake_cleaners: HiddenAds campaign (McAfee Labs, 2022)
        db.execSQL(
            "INSERT OR REPLACE INTO known_adware (packageName, displayName, reason, addedAt) VALUES " +
                "('cn.junk.clean.plp', 'Junk Cleaner', 'HiddenAds adware (McAfee Labs 2022)', $now)," +
                "('com.easy.clean.ipz', 'EasyCleaner', 'HiddenAds adware (McAfee Labs 2022)', $now)," +
                "('com.power.doctor.mnb', 'Power Doctor', 'HiddenAds adware (McAfee Labs 2022)', $now)," +
                "('org.stemp.fll.clean', 'Full Clean - Clean Cache', 'HiddenAds adware (McAfee Labs 2022)', $now)," +
                "('org.qck.cle.oyo', 'Quick Cleaner', 'HiddenAds adware (McAfee Labs 2022)', $now)," +
                "('org.clean.sys.lunch', 'Keep Clean', 'HiddenAds adware (McAfee Labs 2022)', $now)"
        )

        // known_scam_apps: Joker premium-SMS/subscription fraud family (Check Point, 2020)
        db.execSQL(
            "INSERT OR REPLACE INTO known_scam_apps (packageName, displayName, scamType, reason, addedAt) VALUES " +
                "('com.imagecompress.android', 'Image Compress', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.contact.withme.texts', 'Contact With Me Texts', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.hmvoice.friendsms', 'Friend SMS', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.relax.relaxation.androidsms', 'Relaxation SMS', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.cheery.message.sendsms', 'Cheery Message', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.peason.lovinglovemessage', 'Loving Love Message', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.file.recovefiles', 'File Recovery', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.LPlocker.lockapps', 'LP Locker', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.remindme.alram', 'Remind Me Alarm', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)," +
                "('com.training.memorygame', 'Memory Training Game', 'premium_subscription', 'Joker malware (Check Point 2020)', $now)"
        )

        // known_fake_cleaners: HiddenAds cleaner apps (McAfee Labs, 2022)
        db.execSQL(
            "INSERT OR REPLACE INTO known_fake_cleaners (packageName, displayName, reason, addedAt) VALUES " +
                "('com.super.clean.zaz', 'Super Clean', 'HiddenAds fake cleaner (McAfee Labs 2022)', $now)," +
                "('com.fingertip.clean.cvb', 'Fingertip Cleaner', 'HiddenAds fake cleaner (McAfee Labs 2022)', $now)," +
                "('in.phone.clean.www', 'Windy Clean', 'HiddenAds fake cleaner (McAfee Labs 2022)', $now)," +
                "('syn.clean.cool.zbc', 'Cool Clean', 'HiddenAds fake cleaner (McAfee Labs 2022)', $now)," +
                "('in.memory.sys.clean', 'Strong Clean', 'HiddenAds fake cleaner (McAfee Labs 2022)', $now)," +
                "('org.ssl.wind.clean', 'Meteor Clean', 'HiddenAds fake cleaner (McAfee Labs 2022)', $now)"
        )

        // known_fake_antivirus: SharkBot banking-trojan droppers (Check Point, 2022)
        db.execSQL(
            "INSERT OR REPLACE INTO known_fake_antivirus (packageName, displayName, reason, addedAt) VALUES " +
                "('com.abbondioendrizzi.tools.supercleaner', 'Super Cleaner Tools', 'SharkBot dropper (Check Point 2022)', $now)," +
                "('com.abbondioendrizzi.antivirus.supercleaner', 'Antivirus Super Cleaner', 'SharkBot dropper (Check Point 2022)', $now)," +
                "('com.pagnotto28.sellsourcecode.alpha', 'Alpha Antivirus Cleaner', 'SharkBot dropper (Check Point 2022)', $now)," +
                "('com.pagnotto28.sellsourcecode.supercleaner', 'Powerful Cleaner Antivirus', 'SharkBot dropper (Check Point 2022)', $now)," +
                "('com.antivirus.centersecurity.freeforall', 'Center Security Antivirus', 'SharkBot dropper (Check Point 2022)', $now)," +
                "('com.centersecurity.android.cleaner', 'Center Security Cleaner', 'SharkBot dropper (Check Point 2022)', $now)"
        )

        // known_browser_hijackers / known_notification_abusers: no verified public
        // package lists bundled yet — tables stay empty rather than shipping made-up
        // entries. RiskScoringEngine still queries them, so entries can be added later.
    }

    @Provides fun provideAppInfoDao(db: AppDatabase): AppInfoDao = db.appInfoDao()
    @Provides fun provideScanResultDao(db: AppDatabase): ScanResultDao = db.scanResultDao()
    @Provides fun provideNotificationStatsDao(db: AppDatabase): NotificationStatsDao = db.notificationStatsDao()
    @Provides fun provideThreatIntelDao(db: AppDatabase): ThreatIntelDao = db.threatIntelDao()
}
