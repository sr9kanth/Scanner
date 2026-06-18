package com.cleanguard.ai.data.local.dao

import androidx.room.*
import com.cleanguard.ai.data.local.entities.*

@Dao
interface ThreatIntelDao {
    @Query("SELECT * FROM known_adware WHERE packageName = :pkg")
    suspend fun getAdware(pkg: String): KnownAdwareEntity?

    @Query("SELECT * FROM known_scam_apps WHERE packageName = :pkg")
    suspend fun getScamApp(pkg: String): KnownScamAppEntity?

    @Query("SELECT * FROM known_fake_cleaners WHERE packageName = :pkg")
    suspend fun getFakeCleaner(pkg: String): KnownFakeCleanerEntity?

    @Query("SELECT * FROM known_fake_antivirus WHERE packageName = :pkg")
    suspend fun getFakeAntivirus(pkg: String): KnownFakeAntivirusEntity?

    @Query("SELECT * FROM known_browser_hijackers WHERE packageName = :pkg")
    suspend fun getBrowserHijacker(pkg: String): KnownBrowserHijackerEntity?

    @Query("SELECT * FROM known_notification_abusers WHERE packageName = :pkg")
    suspend fun getNotificationAbuser(pkg: String): KnownNotificationAbuserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdware(list: List<KnownAdwareEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScamApps(list: List<KnownScamAppEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFakeCleaners(list: List<KnownFakeCleanerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFakeAntivirus(list: List<KnownFakeAntivirusEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrowserHijackers(list: List<KnownBrowserHijackerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationAbusers(list: List<KnownNotificationAbuserEntity>)
}
