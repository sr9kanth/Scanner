package com.cleanguard.ai.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.cleanguard.ai.data.local.ApiKeyStore
import com.cleanguard.ai.data.local.OnboardingPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore,
    private val onboardingPrefs: OnboardingPrefs
) : ViewModel() {

    fun getGeminiKey(): String = apiKeyStore.getGeminiKey()
    fun getDeepSeekKey(): String = apiKeyStore.getDeepSeekKey()
    fun getVirusTotalKey(): String = apiKeyStore.getVirusTotalKey()

    fun saveKeys(gemini: String, deepSeek: String, virusTotal: String) {
        apiKeyStore.saveKeys(gemini, deepSeek, virusTotal)
    }

    fun completeOnboarding() {
        onboardingPrefs.markComplete()
    }
}
