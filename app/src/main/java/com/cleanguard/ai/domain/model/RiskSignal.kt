package com.cleanguard.ai.domain.model

/**
 * A single human-readable risk contributor with the point value it added to an app's
 * overall risk score. Used by the App Detail "Signals Detected" breakdown.
 */
data class RiskSignal(
    val label: String,
    val points: Int
)
