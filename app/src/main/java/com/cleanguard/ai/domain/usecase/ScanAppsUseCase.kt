package com.cleanguard.ai.domain.usecase

import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.repository.AIRepository
import com.cleanguard.ai.domain.repository.AppScanRepository
import javax.inject.Inject

class ScanAppsUseCase @Inject constructor(
    private val appScanRepository: AppScanRepository,
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(): Result<List<AppInfo>> = runCatching {
        val apps = appScanRepository.scanInstalledApps()

        val toAnalyze = apps
            .filter { it.riskScore in 40..199 }
            .sortedByDescending { it.riskScore }
            .take(15)

        for (app in toAnalyze) {
            aiRepository.analyzeApp(
                appName = app.appName,
                packageName = app.packageName,
                permissions = app.permissions,
                hasAccessibility = app.hasAccessibilityService,
                hasOverlay = app.hasOverlayPermission,
                installSource = app.installerPackage
            ).onSuccess { assessment ->
                val blended = ((assessment.riskScore * 0.6) + (app.riskScore * 0.4)).toInt()
                appScanRepository.updateAiAssessment(app.packageName, assessment, blended)
            }
        }

        apps
    }
}
