package com.cleanguard.ai.domain.repository

import com.cleanguard.ai.domain.model.AIThreatAssessment
import com.cleanguard.ai.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppScanRepository {
    fun getAllApps(): Flow<List<AppInfo>>
    fun getHighRiskApps(): Flow<List<AppInfo>>
    fun searchApps(query: String): Flow<List<AppInfo>>
    fun getAccessibilityApps(): Flow<List<AppInfo>>
    fun getOverlayApps(): Flow<List<AppInfo>>
    suspend fun scanInstalledApps(): List<AppInfo>
    suspend fun getApp(packageName: String): AppInfo?
    suspend fun updateAiAssessment(packageName: String, assessment: AIThreatAssessment, newRiskScore: Int)
}
