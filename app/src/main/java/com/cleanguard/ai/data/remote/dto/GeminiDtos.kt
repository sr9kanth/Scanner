package com.cleanguard.ai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @SerialName("generation_config") val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
data class GeminiContent(
    val role: String = "user",
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float = 0.2f,
    @SerialName("max_output_tokens") val maxOutputTokens: Int = 2048,
    @SerialName("response_mime_type") val responseMimeType: String = "application/json"
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)

@Serializable
data class GeminiThreatJson(
    @SerialName("risk_score") val riskScore: Int,
    val confidence: Double,
    val category: String,
    @SerialName("offending_app") val offendingApp: String? = null,
    @SerialName("recommended_actions") val recommendedActions: List<String>,
    val reasoning: String,
    val explanation: String
)
