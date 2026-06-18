package com.cleanguard.ai.data.repository

import com.cleanguard.ai.data.local.dao.NotificationStatsDao
import com.cleanguard.ai.data.local.entities.NotificationStatsEntity
import com.cleanguard.ai.domain.model.NotificationStats
import com.cleanguard.ai.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationStatsDao: NotificationStatsDao
) : NotificationRepository {

    override fun getAllStats(): Flow<List<NotificationStats>> =
        notificationStatsDao.getAllStats().map { list -> list.map { it.toDomain() } }

    override fun getSpammyApps(): Flow<List<NotificationStats>> =
        notificationStatsDao.getSpammyApps().map { list -> list.map { it.toDomain() } }

    override fun getSpammyCount(): Flow<Int> = notificationStatsDao.getSpammyCount()

    override suspend fun recordNotification(packageName: String, appName: String) {
        val existing = notificationStatsDao.getStats(packageName)
        val now = System.currentTimeMillis()
        if (existing == null) {
            notificationStatsDao.insert(
                NotificationStatsEntity(
                    packageName = packageName,
                    appName = appName,
                    totalCount = 1,
                    dailyAverage = 1f,
                    weeklyCount = 1,
                    lastNotificationAt = now,
                    isSpammy = false
                )
            )
        } else {
            val newWeekly = existing.weeklyCount + 1
            val updated = existing.copy(
                totalCount = existing.totalCount + 1,
                weeklyCount = newWeekly,
                dailyAverage = newWeekly / 7f,
                lastNotificationAt = now,
                isSpammy = newWeekly > 50
            )
            notificationStatsDao.update(updated)
        }
    }

    private fun NotificationStatsEntity.toDomain() = NotificationStats(
        packageName = packageName,
        appName = appName,
        totalCount = totalCount,
        dailyAverage = dailyAverage,
        weeklyCount = weeklyCount,
        lastNotificationAt = lastNotificationAt,
        isSpammy = isSpammy
    )
}
