package com.cleanguard.ai.presentation.apps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.AIThreatAssessment
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.RiskSignal
import com.cleanguard.ai.domain.repository.AIRepository
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.engine.RiskScoringEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppDetailUiState(
    val app: AppInfo? = null,
    val signals: List<RiskSignal> = emptyList(),
    val isLoading: Boolean = true,
    val isQueryingAi: Boolean = false,
    val aiResult: AIThreatAssessment? = null,
    val aiError: String? = null
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val appScanRepository: AppScanRepository,
    private val aiRepository: AIRepository,
    private val riskScoringEngine: RiskScoringEngine
) : ViewModel() {

    private val packageName: String = savedStateHandle["packageName"] ?: ""

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val app = appScanRepository.getApp(packageName)
            val signals = app?.let { riskScoringEngine.describeSignals(it) } ?: emptyList()
            _uiState.update {
                it.copy(
                    app = app,
                    signals = signals,
                    isLoading = false,
                    aiResult = app?.aiAssessment
                )
            }
        }
    }

    fun queryAi() {
        val app = _uiState.value.app ?: return
        if (_uiState.value.isQueryingAi) return
        viewModelScope.launch {
            _uiState.update { it.copy(isQueryingAi = true, aiError = null) }
            aiRepository.analyzeApp(
                appName = app.appName,
                packageName = app.packageName,
                permissions = app.permissions,
                hasAccessibility = app.hasAccessibilityService,
                hasOverlay = app.hasOverlayPermission,
                installSource = app.installerPackage
            ).onSuccess { assessment ->
                _uiState.update { it.copy(isQueryingAi = false, aiResult = assessment) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isQueryingAi = false,
                        aiError = error.message ?: "AI analysis failed. Check your API key and network."
                    )
                }
            }
        }
    }
}
