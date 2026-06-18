package com.cleanguard.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scanTimestamp: Long,
    val totalAppsScanned: Int,
    val highRiskCount: Int,
    val suspiciousCount: Int,
    val reviewCount: Int,
    val safeCount: Int,
    val phoneHealthScore: Int,
    val accessibilityRisksCount: Int,
    val overlayRisksCount: Int,
    val notificationAbusersCount: Int,
    val privacyRisksCount: Int,
    val summaryJson: String
)
