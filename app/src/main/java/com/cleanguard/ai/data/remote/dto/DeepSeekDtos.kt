package com.cleanguard.ai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<DeepSeekMessage>,
    val temperature: Float = 0.2f,
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    @SerialName("response_format") val responseFormat: DeepSeekResponseFormat = DeepSeekResponseFormat()
)

@Serializable
data class DeepSeekMessage(
    val role: String,
    val content: String
)

@Serializable
data class DeepSeekResponseFormat(
    val type: String = "json_object"
)

@Serializable
data class DeepSeekResponse(
    val id: String? = null,
    val choices: List<DeepSeekChoice>? = null,
    val error: DeepSeekError? = null
)

@Serializable
data class DeepSeekChoice(
    val message: DeepSeekMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class DeepSeekError(
    val message: String,
    val type: String? = null
)

@Serializable
data class DeepSeekValidationJson(
    val verdict: String,
    @SerialName("risk_score") val riskScore: Int,
    val confidence: Double,
    @SerialName("additional_concerns") val additionalConcerns: List<String>,
    @SerialName("alternative_recommendations") val alternativeRecommendations: List<String>,
    val reasoning: String
)
