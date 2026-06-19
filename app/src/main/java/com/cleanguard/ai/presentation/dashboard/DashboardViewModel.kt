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

    init {
        observeHealthScore()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            scanAppsUseCase()
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            scanAppsUseCase().fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(isLoading = false, lastScanTimestamp = System.currentTimeMillis())
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
