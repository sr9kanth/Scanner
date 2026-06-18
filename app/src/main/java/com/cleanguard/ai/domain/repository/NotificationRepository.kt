package com.cleanguard.ai.domain.repository

import com.cleanguard.ai.domain.model.NotificationStats
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllStats(): Flow<List<NotificationStats>>
    fun getSpammyApps(): Flow<List<NotificationStats>>
    fun getSpammyCount(): Flow<Int>
    suspend fun recordNotification(packageName: String, appName: String)
}
