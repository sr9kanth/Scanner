package com.cleanguard.ai.presentation.chrome

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChromeUiState(
    val isChromeInstalled: Boolean = false,
    val isChromeEnabled: Boolean = false,
    val currentStep: Int = 0
)

@HiltViewModel
class ChromeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChromeUiState())
    val uiState: StateFlow<ChromeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            checkChromeStatus()
        }
    }

    private fun checkChromeStatus() {
        val pm = context.packageManager
        val isInstalled = try {
            pm.getPackageInfo("com.android.chrome", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) { false }
        val isEnabled = try {
            pm.getApplicationInfo("com.android.chrome", 0).enabled
        } catch (e: Exception) { false }
        _uiState.update { it.copy(isChromeInstalled = isInstalled, isChromeEnabled = isEnabled) }
    }

    fun setStep(step: Int) { _uiState.update { it.copy(currentStep = step) } }
}
