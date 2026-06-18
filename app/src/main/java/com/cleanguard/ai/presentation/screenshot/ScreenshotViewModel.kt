package com.cleanguard.ai.presentation.screenshot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.domain.model.ConsensusAssessment
import com.cleanguard.ai.domain.usecase.AnalyzeScreenshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenshotUiState(
    val selectedImageBytes: ByteArray? = null,
    val isAnalyzing: Boolean = false,
    val assessment: ConsensusAssessment? = null,
    val error: String? = null
)

@HiltViewModel
class ScreenshotViewModel @Inject constructor(
    private val analyzeScreenshotUseCase: AnalyzeScreenshotUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenshotUiState())
    val uiState: StateFlow<ScreenshotUiState> = _uiState.asStateFlow()

    fun setImage(bytes: ByteArray) {
        _uiState.update { it.copy(selectedImageBytes = bytes, assessment = null, error = null) }
    }

    fun analyze(isGrandparentMode: Boolean = false) {
        val bytes = _uiState.value.selectedImageBytes ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }
            analyzeScreenshotUseCase(bytes, isGrandparentMode).fold(
                onSuccess = { assessment ->
                    _uiState.update { it.copy(isAnalyzing = false, assessment = assessment) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isAnalyzing = false, error = e.message ?: "Analysis failed") }
                }
            )
        }
    }

    fun reset() { _uiState.value = ScreenshotUiState() }
}
