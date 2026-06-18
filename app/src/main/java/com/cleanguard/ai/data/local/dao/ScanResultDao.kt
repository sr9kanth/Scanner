package com.cleanguard.ai.data.local.dao

import androidx.room.*
import com.cleanguard.ai.data.local.entities.ScanResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanResultDao {
    @Query("SELECT * FROM scan_results ORDER BY scanTimestamp DESC")
    fun getAllScanResults(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results ORDER BY scanTimestamp DESC LIMIT 1")
    fun getLatestScanResult(): Flow<ScanResultEntity?>

    @Query("SELECT * FROM scan_results WHERE id = :id")
    suspend fun getScanResult(id: Long): ScanResultEntity?

    @Insert
    suspend fun insert(result: ScanResultEntity): Long

    @Query("DELETE FROM scan_results WHERE scanTimestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
