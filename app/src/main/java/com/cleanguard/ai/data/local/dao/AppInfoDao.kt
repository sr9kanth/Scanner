package com.cleanguard.ai.data.local.dao

import androidx.room.*
import com.cleanguard.ai.data.local.entities.AppInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM app_info ORDER BY riskScore DESC")
    fun getAllApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM app_info WHERE riskScore >= 100 ORDER BY riskScore DESC")
    fun getHighRiskApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM app_info WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): AppInfoEntity?

    @Query("SELECT * FROM app_info WHERE appName LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%'")
    fun searchApps(query: String): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM app_info WHERE hasAccessibilityService = 1")
    fun getAccessibilityApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM app_info WHERE hasOverlayPermission = 1")
    fun getOverlayApps(): Flow<List<AppInfoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: AppInfoEntity)

    @Update
    suspend fun update(app: AppInfoEntity)

    @Query("DELETE FROM app_info")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM app_info WHERE riskScore >= 100")
    fun getRemoveImmediatelyCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM app_info WHERE riskScore >= 60")
    fun getSuspiciousCount(): Flow<Int>
}
