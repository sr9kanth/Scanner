package com.cleanguard.ai.domain.usecase

import com.cleanguard.ai.data.local.ApiKeyStore
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.repository.AIRepository
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.domain.repository.VirusTotalRepository
import javax.inject.Inject

class ScanAppsUseCase @Inject constructor(
    private val appScanRepository: AppScanRepository,
    private val aiRepository: AIRepository,
    private val virusTotalRepository: VirusTotalRepository,
    private val apiKeyStore: ApiKeyStore
) {
    suspend operator fun invoke(
        onProgress: (packageName: String, index: Int, total: Int) -> Unit = { _, _, _ -> }
    ): Result<List<AppInfo>> = runCatching {
        val apps = appScanRepository.scanInstalledApps()

        val toAnalyze = apps
            .filter { it.riskScore in 40..199 }
            .sortedByDescending { it.riskScore }
            .take(15)

        val vtKey = apiKeyStore.getVirusTotalKey()

        toAnalyze.forEachIndexed { index, app ->
            onProgress(app.packageName, index, toAnalyze.size)

            var adjustedScore = app.riskScore

            // VirusTotal hash lookup
            if (vtKey.isNotEmpty() && app.apkPath != null) {
                virusTotalRepository.scanByHash(app.apkPath, vtKey).onSuccess { stats ->
                    adjustedScore = when {
                        stats.malicious >= 10 -> maxOf(adjustedScore, 130)
                        stats.malicious in 5..9  -> maxOf(adjustedScore, 100)
                        stats.malicious in 1..4  -> maxOf(adjustedScore, 70)
                        stats.suspicious >= 5    -> maxOf(adjustedScore, 65)
                        stats.harmless > 10 && stats.malicious == 0 -> minOf(adjustedScore, 25)
                        else -> adjustedScore
                    }
                    appScanRepository.updateVirusTotalResult(app.packageName, stats, adjustedScore)
                }
            }

            // Gemini AI analysis
            aiRepository.analyzeApp(
                appName = app.appName,
                packageName = app.packageName,
                permissions = app.permissions,
                hasAccessibility = app.hasAccessibilityService,
                hasOverlay = app.hasOverlayPermission,
                installSource = app.installerPackage
            ).onSuccess { assessment ->
                val blended = ((assessment.riskScore * 0.6) + (adjustedScore * 0.4)).toInt()
                appScanRepository.updateAiAssessment(app.packageName, assessment, blended)
            }
        }

        onProgress("", toAnalyze.size, toAnalyze.size)
        apps
    }
}
