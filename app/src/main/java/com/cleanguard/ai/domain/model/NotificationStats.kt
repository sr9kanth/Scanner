package com.cleanguard.ai.domain.model

data class NotificationStats(
    val packageName: String,
    val appName: String,
    val totalCount: Int,
    val dailyAverage: Float,
    val weeklyCount: Int,
    val lastNotificationAt: Long,
    val isSpammy: Boolean
) {
    companion object {
        fun getSimulationData(): List<NotificationStats> {
            val now = System.currentTimeMillis()
            return listOf(
                NotificationStats(
                    packageName = "com.notification.lucky.rewards",
                    appName = "Lucky Rewards",
                    totalCount = 248,
                    dailyAverage = 248f / 7f,
                    weeklyCount = 248,
                    lastNotificationAt = now,
                    isSpammy = true
                ),
                NotificationStats(
                    packageName = "com.news.popup.daily",
                    appName = "Daily Alert News Feed",
                    totalCount = 112,
                    dailyAverage = 112f / 7f,
                    weeklyCount = 112,
                    lastNotificationAt = now,
                    isSpammy = true
                ),
                NotificationStats(
                    packageName = "com.battery.saver.boost",
                    appName = "Swift Booster & Battery Saver",
                    totalCount = 89,
                    dailyAverage = 89f / 7f,
                    weeklyCount = 89,
                    lastNotificationAt = now,
                    isSpammy = true
                )
            )
        }
    }
}
