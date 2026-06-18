package com.cleanguard.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_stats")
data class NotificationStatsEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val totalCount: Int,
    val dailyAverage: Float,
    val weeklyCount: Int,
    val lastNotificationAt: Long,
    val isSpammy: Boolean
)
