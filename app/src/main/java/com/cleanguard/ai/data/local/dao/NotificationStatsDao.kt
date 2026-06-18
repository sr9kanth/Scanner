package com.cleanguard.ai.data.local.dao

import androidx.room.*
import com.cleanguard.ai.data.local.entities.NotificationStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationStatsDao {
    @Query("SELECT * FROM notification_stats ORDER BY weeklyCount DESC")
    fun getAllStats(): Flow<List<NotificationStatsEntity>>

    @Query("SELECT * FROM notification_stats WHERE isSpammy = 1 ORDER BY weeklyCount DESC")
    fun getSpammyApps(): Flow<List<NotificationStatsEntity>>

    @Query("SELECT * FROM notification_stats WHERE packageName = :packageName")
    suspend fun getStats(packageName: String): NotificationStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: NotificationStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<NotificationStatsEntity>)

    @Update
    suspend fun update(stats: NotificationStatsEntity)

    @Query("SELECT COUNT(*) FROM notification_stats WHERE isSpammy = 1")
    fun getSpammyCount(): Flow<Int>
}
