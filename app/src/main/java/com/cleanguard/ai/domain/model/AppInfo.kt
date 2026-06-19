package com.cleanguard.ai.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val installDate: Long,
    val lastUpdateDate: Long,
    val installerPackage: String?,
    val isEnabled: Boolean,
    val permissions: List<String>,
    val category: Int,
    val sizeBytes: Long,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val hasAccessibilityService: Boolean,
    val hasOverlayPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isSystemApp: Boolean,
    val apkPath: String? = null,
    val aiAssessment: AIThreatAssessment? = null
)

enum class RiskLevel(val label: String, val parentFriendlyLabel: String) {
    SAFE("Safe", "This app looks safe"),
    REVIEW("Review", "We'd like you to take a look at this app"),
    SUSPICIOUS("Suspicious", "This app has some worrying signs"),
    REMOVE_IMMEDIATELY("Remove Immediately", "We strongly recommend removing this app now");

    companion object {
        fun fromScore(score: Int) = when {
            score >= 100 -> REMOVE_IMMEDIATELY
            score >= 60 -> SUSPICIOUS
            score >= 30 -> REVIEW
            else -> SAFE
        }
    }
}
