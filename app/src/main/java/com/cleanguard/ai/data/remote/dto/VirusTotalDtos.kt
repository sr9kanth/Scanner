package com.cleanguard.ai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VirusTotalResponse(
    val data: VirusTotalData? = null,
    val error: VirusTotalError? = null
)

@Serializable
data class VirusTotalData(
    val id: String,
    val type: String,
    val attributes: VirusTotalAttributes
)

@Serializable
data class VirusTotalAttributes(
    @SerialName("last_analysis_stats") val lastAnalysisStats: VirusTotalStats,
    @SerialName("last_analysis_results") val lastAnalysisResults: Map<String, VirusTotalVendorResult>? = null,
    val meaningful_name: String? = null
)

@Serializable
data class VirusTotalStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val undetected: Int = 0,
    val harmless: Int = 0,
    @SerialName("type-unsupported") val typeUnsupported: Int = 0
)

@Serializable
data class VirusTotalVendorResult(
    val category: String,
    val result: String? = null,
    @SerialName("engine_name") val engineName: String
)

@Serializable
data class VirusTotalError(
    val code: String,
    val message: String
)
