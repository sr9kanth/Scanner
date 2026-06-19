package com.cleanguard.ai.domain.model

data class PhoneHealthScore(
    val score: Int,
    val grade: HealthGrade,
    val highRiskApps: Int,
    val notificationAbusers: Int,
    val accessibilityRisks: Int,
    val overlayRisks: Int,
    val privacyRisks: Int,
    val lastScanTimestamp: Long?
) {
    companion object {
        fun calculate(
            highRiskApps: Int,
            notificationAbusers: Int,
            accessibilityRisks: Int,
            overlayRisks: Int,
            privacyRisks: Int,
            lastScanTimestamp: Long?
        ): PhoneHealthScore {
            val deduction = (highRiskApps * 25) + (accessibilityRisks * 10) + (overlayRisks * 10)
            val finalScore = (100 - deduction).coerceIn(0, 100)
            return PhoneHealthScore(
                score = finalScore,
                grade = HealthGrade.fromScore(finalScore),
                highRiskApps = highRiskApps,
                notificationAbusers = notificationAbusers,
                accessibilityRisks = accessibilityRisks,
                overlayRisks = overlayRisks,
                privacyRisks = privacyRisks,
                lastScanTimestamp = lastScanTimestamp
            )
        }
    }
}

enum class HealthGrade(val label: String, val emoji: String, val color: Long) {
    EXCELLENT("Excellent", "🟢", 0xFF4CAF50),
    GOOD("Good", "🟡", 0xFF8BC34A),
    NEEDS_ATTENTION("Needs Attention", "🟠", 0xFFFF9800),
    AT_RISK("At Risk", "🔴", 0xFFF44336);

    companion object {
        fun fromScore(score: Int) = when {
            score >= 85 -> EXCELLENT
            score >= 65 -> GOOD
            score >= 40 -> NEEDS_ATTENTION
            else -> AT_RISK
        }
    }
}
