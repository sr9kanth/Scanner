package com.cleanguard.ai.data.local

import android.content.Context
import com.cleanguard.ai.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs = context.getSharedPreferences("cleanguard_prefs", Context.MODE_PRIVATE)

    // SharedPreferences takes priority (set via Settings or Onboarding).
    // Falls back to keys baked in at build time from local.properties / CI env vars.
    fun getGeminiKey(): String =
        prefs.getString("gemini_key", "")?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.GEMINI_API_KEY

    fun getDeepSeekKey(): String =
        prefs.getString("deepseek_key", "")?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.DEEPSEEK_API_KEY

    fun getVirusTotalKey(): String =
        prefs.getString("virustotal_key", "")?.takeIf { it.isNotEmpty() }
            ?: BuildConfig.VIRUSTOTAL_API_KEY

    fun saveKeys(gemini: String, deepSeek: String, virusTotal: String) {
        prefs.edit()
            .putString("gemini_key", gemini)
            .putString("deepseek_key", deepSeek)
            .putString("virustotal_key", virusTotal)
            .apply()
    }
}
