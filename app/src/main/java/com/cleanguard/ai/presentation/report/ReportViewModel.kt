package com.cleanguard.ai.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.NotificationStats
import com.cleanguard.ai.domain.model.PhoneHealthScore
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.domain.repository.NotificationRepository
import com.cleanguard.ai.domain.usecase.GetPhoneHealthScoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class ReportUiState(
    val healthScore: PhoneHealthScore? = null,
    val highRiskApps: List<AppInfo> = emptyList(),
    val notificationAbusers: List<NotificationStats> = emptyList(),
    val accessibilityApps: List<AppInfo> = emptyList(),
    val overlayApps: List<AppInfo> = emptyList()
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getPhoneHealthScoreUseCase: GetPhoneHealthScoreUseCase,
    private val appScanRepository: AppScanRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val uiState: StateFlow<ReportUiState> = combine(
        getPhoneHealthScoreUseCase(),
        appScanRepository.getHighRiskApps(),
        notificationRepository.getSpammyApps(),
        appScanRepository.getAccessibilityApps(),
        appScanRepository.getOverlayApps()
    ) { score, highRisk, spammy, accessibility, overlay ->
        ReportUiState(
            healthScore = score,
            highRiskApps = highRisk.filter { it.riskLevel == RiskLevel.REMOVE_IMMEDIATELY || it.riskLevel == RiskLevel.SUSPICIOUS },
            notificationAbusers = spammy,
            accessibilityApps = accessibility,
            overlayApps = overlay
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())

    fun buildReportText(): String {
        val state = uiState.value
        val score = state.healthScore?.score ?: 0
        val label = (state.healthScore?.grade ?: com.cleanguard.ai.domain.model.HealthGrade.AT_RISK).label
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(java.util.Date())

        val auditedCount = state.healthScore?.let {
            maxOf(it.highRiskApps, state.highRiskApps.size) +
                state.accessibilityApps.size + state.overlayApps.size
        } ?: (state.highRiskApps.size + state.accessibilityApps.size + state.overlayApps.size)

        return buildString {
            appendLine("CLEANGUARD AI - SECURITY DIAGNOSTIC REPORT")
            appendLine("Date: $dateStr")
            appendLine("OVERALL PHONE HEALTH SCORE: $score/100 - [$label]")
            appendLine()
            appendLine("Installed Apps Audited: $auditedCount")
            appendLine("Alert Threats Flagged: ${state.highRiskApps.size}")
            appendLine("Screen Accessibility Access: ${state.accessibilityApps.size} apps")
            appendLine("Active Window overlays: ${state.overlayApps.size} apps")
            appendLine("Spam Alert notifications: ${state.notificationAbusers.size} spammers")

            if (state.highRiskApps.isNotEmpty()) {
                appendLine()
                appendLine("WARNING: HIGH RISK APPLICATIONS DETECTED:")
                state.highRiskApps.forEach { app ->
                    appendLine(
                        "• ${app.appName} (${app.packageName}) - Threat Level: " +
                            "${app.riskLevel.label} (Score: ${app.riskScore}/100)"
                    )
                    val reason = app.aiAssessment?.explanation?.takeIf { it.isNotBlank() }
                        ?: "Multiple high-risk signals"
                    appendLine("  Reason: $reason")
                }
            }
        }.trimEnd()
    }
}
