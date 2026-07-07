package com.cleanguard.ai.engine

import android.Manifest
import com.cleanguard.ai.data.local.dao.ThreatIntelDao
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.RiskSignal
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

        val dangerousCount = permissions.count { it in DANGEROUS_PERMISSIONS }
        if (dangerousCount >= 5) score += 10

        score += threatBonus

        return score.coerceIn(0, 200)
    }

    /**
     * Produces a human-readable breakdown of the signals that contributed to an app's risk
     * score. Mirrors the point values applied in [calculateScore], including threat-DB hits.
     */
    suspend fun describeSignals(app: AppInfo): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()

        threatIntelDao.getAdware(app.packageName)?.let {
            signals += RiskSignal("Listed in known adware database", 80)
        }
        threatIntelDao.getScamApp(app.packageName)?.let {
            signals += RiskSignal("Listed in known scam-app database", 100)
        }
        threatIntelDao.getFakeCleaner(app.packageName)?.let {
            signals += RiskSignal("Listed in known fake-cleaner database", 90)
        }
        threatIntelDao.getFakeAntivirus(app.packageName)?.let {
            signals += RiskSignal("Listed in known fake-antivirus database", 90)
        }
        threatIntelDao.getBrowserHijacker(app.packageName)?.let {
            signals += RiskSignal("Listed in known browser-hijacker database", 85)
        }
        threatIntelDao.getNotificationAbuser(app.packageName)?.let {
            signals += RiskSignal("Listed in known notification-abuser database", 50)
        }

        if (isTrustedSystemPackage(app.packageName, app.isSystemApp) && signals.isEmpty()) {
            return listOf(RiskSignal("Trusted system / OEM app", 0))
        }

        if (app.hasAccessibilityService) signals += RiskSignal("Accessibility service is enabled", 40)
        if (app.hasOverlayPermission) signals += RiskSignal("Can draw over other apps (overlay granted)", 30)

        val installSource = app.installerPackage
        val isFromPlayStore = installSource == "com.android.vending"
        if (!isFromPlayStore && installSource != null) {
            signals += RiskSignal("Installed from outside the Play Store", 30)
        }
        if (installSource == null && !app.isSystemApp) {
            signals += RiskSignal("Unknown install source (side-loaded)", 30)
        }

        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        if (app.installDate > thirtyDaysAgo) signals += RiskSignal("Recently installed (last 30 days)", 15)

        if (app.permissions.size >= 20) signals += RiskSignal("Requests an unusually large number of permissions", 15)
        if (app.hasNotificationPermission) signals += RiskSignal("Can post notifications", 10)

        val dangerousCount = app.permissions.count { it in DANGEROUS_PERMISSIONS }
        if (dangerousCount >= 5) signals += RiskSignal("Holds $dangerousCount sensitive permissions", 10)

        return signals
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

    companion object {
        private val DANGEROUS_PERMISSIONS = setOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CALL_LOG,
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.CHANGE_NETWORK_STATE"
        )
    }
}
