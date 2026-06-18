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
}
