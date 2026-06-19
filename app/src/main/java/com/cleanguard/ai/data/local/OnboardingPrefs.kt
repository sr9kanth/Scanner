package com.cleanguard.ai.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingPrefs @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("cleanguard_prefs", Context.MODE_PRIVATE)

    fun isOnboardingComplete(): Boolean = prefs.getBoolean("onboarding_complete", false)

    fun markComplete() {
        prefs.edit().putBoolean("onboarding_complete", true).apply()
    }
}
