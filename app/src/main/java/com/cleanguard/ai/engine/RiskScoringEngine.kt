package com.cleanguard.ai.engine

import android.Manifest
import com.cleanguard.ai.data.local.dao.ThreatIntelDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskScoringEngine @Inject constructor(
    private val threatIntelDao: ThreatIntelDao
) {
    suspend fun calculateScore(
        permissions: List<String>,
        installSource: String?,
        installDate: Long,
        hasAccessibility: Boolean,
        hasOverlay: Boolean,
        hasNotification: Boolean,
        packageName: String,
        isSystemApp: Boolean = false
    ): Int {
        val threatBonus = getThreatIntelBonus(packageName)

        // Trusted OEM/platform packages are safe unless our threat DB flags them
        if (isTrustedSystemPackage(packageName, isSystemApp) && threatBonus == 0) {
            return 5
        }

        var score = 0

        if (hasAccessibility) score += 40
        if (hasOverlay) score += 30

        val isFromPlayStore = installSource == "com.android.vending"
        if (!isFromPlayStore && installSource != null) score += 30
        // Pre-installed system apps legitimately have no install source — don't penalise them
        if (installSource == null && !isSystemApp) score += 30

        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        if (installDate > thirtyDaysAgo) score += 15

        if (permissions.size >= 20) score += 15
        if (hasNotification) score += 10

        val dangerousPermissions = setOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALL_LOG,
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.CHANGE_NETWORK_STATE"
        )
        val dangerousCount = permissions.count { it in dangerousPermissions }
        if (dangerousCount >= 5) score += 10

        score += threatBonus

        return score.coerceIn(0, 200)
    }

    private fun isTrustedSystemPackage(packageName: String, isSystemApp: Boolean): Boolean {
        if (!isSystemApp) return false
        val trustedPrefixes = listOf(
            "com.google.",
            "com.android.",
            "android",
            "com.samsung.",
            "com.sec.",
            "com.qualcomm.",
            "com.oneplus.",
            "com.miui.",
            "com.huawei.",
            "com.lge.",
            "com.motorola.",
            "com.htc.",
            "com.sony.",
            "com.asus.",
            "com.oppo.",
            "com.vivo.",
            "com.realme.",
            "com.nothing."
        )
        return trustedPrefixes.any { packageName.startsWith(it) }
    }

    private suspend fun getThreatIntelBonus(packageName: String): Int {
        var bonus = 0
        if (threatIntelDao.getAdware(packageName) != null) bonus += 80
        if (threatIntelDao.getScamApp(packageName) != null) bonus += 100
        if (threatIntelDao.getFakeCleaner(packageName) != null) bonus += 90
        if (threatIntelDao.getFakeAntivirus(packageName) != null) bonus += 90
        if (threatIntelDao.getBrowserHijacker(packageName) != null) bonus += 85
        if (threatIntelDao.getNotificationAbuser(packageName) != null) bonus += 50
        return bonus
    }
}
