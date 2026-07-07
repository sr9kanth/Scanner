package com.cleanguard.ai.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.cleanguard.ai.data.local.dao.AppInfoDao
import com.cleanguard.ai.data.local.entities.AppInfoEntity
import com.cleanguard.ai.data.remote.dto.VirusTotalStats
import com.cleanguard.ai.domain.model.*
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.engine.RiskScoringEngine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appInfoDao: AppInfoDao,
    private val riskScoringEngine: RiskScoringEngine
) : AppScanRepository {

    override fun getAllApps(): Flow<List<AppInfo>> =
        appInfoDao.getAllApps().map { list -> list.map { it.toAppInfo() } }

    override fun getHighRiskApps(): Flow<List<AppInfo>> =
        appInfoDao.getHighRiskApps().map { list -> list.map { it.toAppInfo() } }

    override fun searchApps(query: String): Flow<List<AppInfo>> =
        appInfoDao.searchApps(query).map { list -> list.map { it.toAppInfo() } }

    override fun getAccessibilityApps(): Flow<List<AppInfo>> =
        appInfoDao.getAccessibilityApps().map { list -> list.map { it.toAppInfo() } }

    override fun getOverlayApps(): Flow<List<AppInfo>> =
        appInfoDao.getOverlayApps().map { list -> list.map { it.toAppInfo() } }

    override suspend fun scanInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val infos = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { !it.isSystemApp() || it.hasInterestingPermissions(pm) }
        val apps = buildList { for (info in infos) add(buildAppInfo(pm, info)) }

        appInfoDao.deleteAll()
        appInfoDao.insertAll(apps.map { it.toEntity() })
        return apps
    }

    override suspend fun getApp(packageName: String): AppInfo? =
        appInfoDao.getApp(packageName)?.toAppInfo()

    override suspend fun updateAiAssessment(packageName: String, assessment: AIThreatAssessment, newRiskScore: Int) {
        val entity = appInfoDao.getApp(packageName) ?: return
        val updated = entity.copy(
            riskScore = newRiskScore,
            riskCategory = RiskLevel.fromScore(newRiskScore).name,
            aiAssessmentJson = Json.encodeToString(assessment)
        )
        appInfoDao.update(updated)
    }

    override suspend fun updateVirusTotalResult(packageName: String, stats: VirusTotalStats, newRiskScore: Int) {
        val entity = appInfoDao.getApp(packageName) ?: return
        val updated = entity.copy(
            riskScore = newRiskScore,
            riskCategory = RiskLevel.fromScore(newRiskScore).name,
            virusTotalResult = Json.encodeToString(stats)
        )
        appInfoDao.update(updated)
    }

    private suspend fun buildAppInfo(pm: PackageManager, info: ApplicationInfo): AppInfo {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(info.packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(info.packageName, PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) { null }

        val permissions = packageInfo?.requestedPermissions?.toList() ?: emptyList()
        val installSource = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                pm.getInstallSourceInfo(info.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                pm.getInstallerPackageName(info.packageName)
            }
        } catch (e: Exception) { null }

        val hasAccessibility = hasActiveAccessibilityService(info.packageName)
        val hasOverlay = permissions.contains(android.Manifest.permission.SYSTEM_ALERT_WINDOW) &&
            isOverlayGranted(info)
        val hasNotification = permissions.contains(android.Manifest.permission.POST_NOTIFICATIONS)

        val riskScore = riskScoringEngine.calculateScore(
            permissions = permissions,
            installSource = installSource,
            installDate = packageInfo?.firstInstallTime ?: 0L,
            hasAccessibility = hasAccessibility,
            hasOverlay = hasOverlay,
            hasNotification = hasNotification,
            packageName = info.packageName,
            isSystemApp = info.isSystemApp()
        )

        return AppInfo(
            packageName = info.packageName,
            appName = pm.getApplicationLabel(info).toString(),
            versionName = packageInfo?.versionName ?: "",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                packageInfo?.longVersionCode ?: 0L else packageInfo?.versionCode?.toLong() ?: 0L,
            installDate = packageInfo?.firstInstallTime ?: 0L,
            lastUpdateDate = packageInfo?.lastUpdateTime ?: 0L,
            installerPackage = installSource,
            isEnabled = info.enabled,
            permissions = permissions,
            category = info.category,
            sizeBytes = info.sourceDir?.let { java.io.File(it).length() } ?: 0L,
            riskScore = riskScore,
            riskLevel = RiskLevel.fromScore(riskScore),
            hasAccessibilityService = hasAccessibility,
            hasOverlayPermission = hasOverlay,
            hasNotificationPermission = hasNotification,
            isSystemApp = info.isSystemApp(),
            apkPath = info.sourceDir
        )
    }

    private fun hasActiveAccessibilityService(packageName: String): Boolean {
        val settingValue = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return settingValue.split(":").any {
            android.content.ComponentName.unflattenFromString(it)?.packageName == packageName
        }
    }

    /**
     * Checks whether the "draw over other apps" capability is actually granted to the app,
     * not merely requested in its manifest.
     */
    private fun isOverlayGranted(info: ApplicationInfo): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        return try {
            when (appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, info.uid, info.packageName
            )) {
                android.app.AppOpsManager.MODE_ALLOWED -> true
                android.app.AppOpsManager.MODE_DEFAULT -> context.packageManager.checkPermission(
                    android.Manifest.permission.SYSTEM_ALERT_WINDOW, info.packageName
                ) == PackageManager.PERMISSION_GRANTED
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun ApplicationInfo.isSystemApp() =
        flags and ApplicationInfo.FLAG_SYSTEM != 0

    private fun ApplicationInfo.hasInterestingPermissions(pm: PackageManager): Boolean {
        val pkg = try {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        } catch (e: Exception) { return false }
        val dangerousPerms = setOf(
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,
            android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        return pkg.requestedPermissions?.any { it in dangerousPerms } == true
    }

    private fun AppInfo.toEntity() = AppInfoEntity(
        packageName = packageName,
        appName = appName,
        versionName = versionName,
        versionCode = versionCode,
        installDate = installDate,
        lastUpdateDate = lastUpdateDate,
        installerPackage = installerPackage,
        isEnabled = isEnabled,
        permissionsJson = Json.encodeToString(permissions),
        category = category,
        sizeBytes = sizeBytes,
        riskScore = riskScore,
        riskCategory = riskLevel.name,
        hasAccessibilityService = hasAccessibilityService,
        hasOverlayPermission = hasOverlayPermission,
        hasNotificationPermission = hasNotificationPermission,
        isSystemApp = isSystemApp,
        lastScannedAt = System.currentTimeMillis(),
        virusTotalResult = null,
        aiAssessmentJson = null
    )

    private fun AppInfoEntity.toAppInfo() = AppInfo(
        packageName = packageName,
        appName = appName,
        versionName = versionName,
        versionCode = versionCode,
        installDate = installDate,
        lastUpdateDate = lastUpdateDate,
        installerPackage = installerPackage,
        isEnabled = isEnabled,
        permissions = try { Json.decodeFromString(permissionsJson) } catch (e: Exception) { emptyList() },
        category = category,
        sizeBytes = sizeBytes,
        riskScore = riskScore,
        riskLevel = RiskLevel.fromScore(riskScore),
        hasAccessibilityService = hasAccessibilityService,
        hasOverlayPermission = hasOverlayPermission,
        hasNotificationPermission = hasNotificationPermission,
        isSystemApp = isSystemApp
    )
}
