package com.cleanguard.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "known_adware")
data class KnownAdwareEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "known_scam_apps")
data class KnownScamAppEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val scamType: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "known_fake_cleaners")
data class KnownFakeCleanerEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "known_fake_antivirus")
data class KnownFakeAntivirusEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "known_browser_hijackers")
data class KnownBrowserHijackerEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "known_notification_abusers")
data class KnownNotificationAbuserEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val reason: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exodus_trackers")
data class ExodusTrackerEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val trackerCount: Int
)
