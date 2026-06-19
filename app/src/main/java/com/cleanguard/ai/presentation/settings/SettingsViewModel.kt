package com.cleanguard.ai.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanguard.ai.data.local.ApiKeyStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isGrandparentMode: Boolean = false,
    val isParentMode: Boolean = false,
    val isDarkMode: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val geminiKey: String = "",
    val deepSeekKey: String = "",
    val virusTotalKey: String = "",
    val keysSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    private val prefs = context.getSharedPreferences("cleanguard_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isGrandparentMode = prefs.getBoolean("grandparent_mode", false),
            isParentMode = prefs.getBoolean("parent_mode", false),
            geminiKey = apiKeyStore.getGeminiKey(),
            deepSeekKey = apiKeyStore.getDeepSeekKey(),
            virusTotalKey = apiKeyStore.getVirusTotalKey()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setGrandparentMode(enabled: Boolean) {
        prefs.edit().putBoolean("grandparent_mode", enabled).apply()
        _uiState.update { it.copy(isGrandparentMode = enabled) }
    }

    fun setParentMode(enabled: Boolean) {
        prefs.edit().putBoolean("parent_mode", enabled).apply()
        _uiState.update { it.copy(isParentMode = enabled) }
    }

    fun saveApiKeys(gemini: String, deepSeek: String, virusTotal: String) {
        apiKeyStore.saveKeys(gemini, deepSeek, virusTotal)
        _uiState.update { it.copy(geminiKey = gemini, deepSeekKey = deepSeek, virusTotalKey = virusTotal, keysSaved = true) }
    }
}
