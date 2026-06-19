package com.cleanguard.ai.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs = context.getSharedPreferences("cleanguard_prefs", Context.MODE_PRIVATE)

    fun getGeminiKey(): String = prefs.getString("gemini_key", "") ?: ""
    fun getDeepSeekKey(): String = prefs.getString("deepseek_key", "") ?: ""
    fun getVirusTotalKey(): String = prefs.getString("virustotal_key", "") ?: ""

    fun saveKeys(gemini: String, deepSeek: String, virusTotal: String) {
        prefs.edit()
            .putString("gemini_key", gemini)
            .putString("deepseek_key", deepSeek)
            .putString("virustotal_key", virusTotal)
            .apply()
    }
}
