package com.cleanguard.ai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
data class AppInfoEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val installDate: Long,
    val lastUpdateDate: Long,
    val installerPackage: String?,
    val isEnabled: Boolean,
    val permissionsJson: String,
    val category: Int,
    val sizeBytes: Long,
    val riskScore: Int,
    val riskCategory: String,
    val hasAccessibilityService: Boolean,
    val hasOverlayPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isSystemApp: Boolean,
    val lastScannedAt: Long,
    val virusTotalResult: String?,
    val aiAssessmentJson: String?
)
