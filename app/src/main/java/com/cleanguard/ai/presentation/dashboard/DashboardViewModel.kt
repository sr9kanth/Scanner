package com.cleanguard.ai.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.PhoneHealthScore
import com.cleanguard.ai.domain.usecase.GetPhoneHealthScoreUseCase
import com.cleanguard.ai.domain.usecase.ScanAppsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val healthScore: PhoneHealthScore? = null,
    val isLoading: Boolean = false,
    val lastScanTimestamp: Long? = null,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getPhoneHealthScoreUseCase: GetPhoneHealthScoreUseCase,
    private val scanAppsUseCase: ScanAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _currentlyScanningApp = MutableStateFlow("")
    val currentlyScanningApp: StateFlow<String> = _currentlyScanningApp.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        observeHealthScore()
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f
            _uiState.update { it.copy(isLoading = true) }
            scanAppsUseCase { packageName, index, total ->
                _currentlyScanningApp.value = packageName
                _scanProgress.value = if (total > 0) index.toFloat() / total.toFloat() else 0f
            }
            _scanProgress.value = 1f
            _isScanning.value = false
            _currentlyScanningApp.value = ""
            _uiState.update { it.copy(isLoading = false, lastScanTimestamp = System.currentTimeMillis()) }
        }
    }

    private fun observeHealthScore() {
        getPhoneHealthScoreUseCase()
            .onEach { score ->
                _uiState.update { it.copy(healthScore = score) }
            }
            .launchIn(viewModelScope)
    }

    fun startScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f
            _uiState.update { it.copy(isLoading = true, error = null) }
            scanAppsUseCase { packageName, index, total ->
                _currentlyScanningApp.value = packageName
                _scanProgress.value = if (total > 0) index.toFloat() / total.toFloat() else 0f
            }.fold(
                onSuccess = {
                    _scanProgress.value = 1f
                    _isScanning.value = false
                    _currentlyScanningApp.value = ""
                    _uiState.update { state ->
                        state.copy(isLoading = false, lastScanTimestamp = System.currentTimeMillis())
                    }
                },
                onFailure = { e ->
                    _isScanning.value = false
                    _currentlyScanningApp.value = ""
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
