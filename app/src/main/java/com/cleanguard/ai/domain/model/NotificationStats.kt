package com.cleanguard.ai.domain.model

data class NotificationStats(
    val packageName: String,
    val appName: String,
    val totalCount: Int,
    val dailyAverage: Float,
    val weeklyCount: Int,
    val lastNotificationAt: Long,
    val isSpammy: Boolean
)
