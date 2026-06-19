package com.cleanguard.ai.presentation.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.usecase.ScanAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanPhase { IDLE, SCANNING_APPS, CHECKING_THREATS, ANALYZING, COMPLETE }

data class ScannerUiState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val progress: Float = 0f,
    val currentApp: String = "",
    val scannedApps: List<AppInfo> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanAppsUseCase: ScanAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ScanPhase.SCANNING_APPS, progress = 0.1f, error = null) }

            _uiState.update { it.copy(phase = ScanPhase.CHECKING_THREATS, progress = 0.4f, currentApp = "Checking threat database...") }

            scanAppsUseCase { packageName, index, total ->
                // Map per-app progress into the 0.4f..0.8f band of the overall scan
                val frac = if (total > 0) index.toFloat() / total.toFloat() else 0f
                _uiState.update {
                    it.copy(
                        phase = ScanPhase.CHECKING_THREATS,
                        progress = 0.4f + frac * 0.4f,
                        currentApp = packageName.ifBlank { it.currentApp }
                    )
                }
            }.fold(
                onSuccess = { apps ->
                    _uiState.update { it.copy(phase = ScanPhase.ANALYZING, progress = 0.8f, currentApp = "Finalizing results...") }
                    _uiState.update { it.copy(phase = ScanPhase.COMPLETE, progress = 1f, scannedApps = apps, currentApp = "") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(phase = ScanPhase.IDLE, error = e.message) }
                }
            )
        }
    }

    fun reset() {
        _uiState.value = ScannerUiState()
    }
}
