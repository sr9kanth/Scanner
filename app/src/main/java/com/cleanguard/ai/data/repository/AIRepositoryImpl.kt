package com.cleanguard.ai.data.repository

import android.util.Base64
import com.cleanguard.ai.BuildConfig
import com.cleanguard.ai.data.local.ApiKeyStore
import com.cleanguard.ai.data.remote.api.DeepSeekApiService
import com.cleanguard.ai.data.remote.api.GeminiApiService
import com.cleanguard.ai.data.remote.dto.*
import com.cleanguard.ai.domain.model.AIThreatAssessment
import com.cleanguard.ai.domain.repository.AIRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val geminiApi: GeminiApiService,
    private val deepSeekApi: DeepSeekApiService,
    private val apiKeyStore: ApiKeyStore
) : AIRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private fun geminiKey() = apiKeyStore.getGeminiKey().ifEmpty { BuildConfig.GEMINI_API_KEY }
    private fun deepSeekKey() = apiKeyStore.getDeepSeekKey().ifEmpty { BuildConfig.DEEPSEEK_API_KEY }

    override suspend fun analyzeApp(
        appName: String,
        packageName: String,
        permissions: List<String>,
        hasAccessibility: Boolean,
        hasOverlay: Boolean,
        installSource: String?
    ): Result<AIThreatAssessment> = runCatching {
        val key = geminiKey()
        require(key.isNotEmpty()) { "No Gemini API key configured" }

        val dangerousPerms = permissions.filter { p ->
            p.contains("CAMERA") || p.contains("MICROPHONE") || p.contains("RECORD_AUDIO") ||
            p.contains("READ_SMS") || p.contains("READ_CONTACTS") || p.contains("LOCATION") ||
            p.contains("READ_CALL_LOG") || p.contains("BIND_DEVICE_ADMIN") ||
            p.contains("SYSTEM_ALERT_WINDOW") || p.contains("BIND_ACCESSIBILITY_SERVICE") ||
            p.contains("PROCESS_OUTGOING_CALLS")
        }.take(10)

        val prompt = """You are a mobile security expert. Analyze this Android app for malware, spyware, or scam behavior.

App Name: $appName
Package: $packageName
Install Source: ${installSource ?: "Pre-installed / Unknown"}
Has Accessibility Service: $hasAccessibility
Has Overlay Permission: $hasOverlay
Total Permissions: ${permissions.size}
Notable Permissions: ${dangerousPerms.joinToString(", ").ifEmpty { "none" }}

Return ONLY valid JSON with this exact structure:
{"risk_score": <0-100>, "confidence": <0.0-1.0>, "category": "<Safe|Adware|Spyware|Scam|Malware|Unknown>", "explanation": "<1-2 sentences for a non-technical user>", "reasoning": "<technical reasoning>", "recommended_actions": ["<action1>", "<action2>"]}"""

        val response = geminiApi.generateContent(
            model = "gemini-2.5-flash",
            apiKey = key,
            request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig()
            )
        )

        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Empty Gemini response")
        val parsed = json.decodeFromString<GeminiThreatJson>(text)
        AIThreatAssessment(
            source = "Gemini 2.5 Flash",
            riskScore = parsed.riskScore.coerceIn(0, 100),
            confidence = parsed.confidence.coerceIn(0.0, 1.0),
            category = parsed.category,
            explanation = parsed.explanation,
            recommendations = parsed.recommendedActions,
            reasoning = parsed.reasoning
        )
    }

    override suspend fun analyzeWithGemini(
        prompt: String,
        imageBytes: ByteArray?
    ): Result<AIThreatAssessment> = runCatching {
        val parts = mutableListOf<GeminiPart>()
        if (imageBytes != null) {
            parts.add(GeminiPart(
                inlineData = GeminiInlineData(
                    mimeType = "image/jpeg",
                    data = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                )
            ))
        }
        parts.add(GeminiPart(text = prompt))

        val response = geminiApi.generateContent(
            model = "gemini-2.5-flash",
            apiKey = geminiKey(),
            request = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts)),
                generationConfig = GeminiGenerationConfig()
            )
        )

        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Empty Gemini response")
        val parsed = json.decodeFromString<GeminiThreatJson>(text)
        AIThreatAssessment(
            source = "Gemini 2.5 Flash",
            riskScore = parsed.riskScore.coerceIn(0, 100),
            confidence = parsed.confidence.coerceIn(0.0, 1.0),
            category = parsed.category,
            explanation = parsed.explanation,
            recommendations = parsed.recommendedActions,
            reasoning = parsed.reasoning
        )
    }

    override suspend fun validateWithDeepSeek(
        originalPrompt: String,
        geminiAssessment: AIThreatAssessment
    ): Result<AIThreatAssessment> = runCatching {
        val key = deepSeekKey()
        require(key.isNotEmpty()) { "No DeepSeek API key configured" }

        val systemPrompt = """You are a mobile security expert validating an AI threat assessment.
            Review the findings and return JSON with fields:
            verdict (agree/disagree/partial), risk_score (0-100), confidence (0.0-1.0),
            additional_concerns (array), alternative_recommendations (array), reasoning (string)."""

        val userMessage = """Original analysis: $originalPrompt

            Gemini Assessment:
            Risk Score: ${geminiAssessment.riskScore}
            Category: ${geminiAssessment.category}
            Explanation: ${geminiAssessment.explanation}
            Recommendations: ${geminiAssessment.recommendations.joinToString(", ")}

            Please validate and provide your assessment."""

        val response = deepSeekApi.chatCompletions(
            authorization = "Bearer $key",
            request = DeepSeekRequest(
                messages = listOf(
                    DeepSeekMessage("system", systemPrompt),
                    DeepSeekMessage("user", userMessage)
                )
            )
        )
        val text = response.choices?.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Empty DeepSeek response")
        val parsed = json.decodeFromString<DeepSeekValidationJson>(text)
        AIThreatAssessment(
            source = "DeepSeek Chat",
            riskScore = parsed.riskScore.coerceIn(0, 100),
            confidence = parsed.confidence.coerceIn(0.0, 1.0),
            category = geminiAssessment.category,
            explanation = parsed.reasoning,
            recommendations = parsed.alternativeRecommendations.ifEmpty { geminiAssessment.recommendations },
            reasoning = parsed.reasoning
        )
    }

    override suspend fun generatePlainEnglishExplanation(
        technicalDetails: String,
        isGrandparentMode: Boolean
    ): Result<String> = runCatching {
        val audience = if (isGrandparentMode)
            "a grandparent with no tech knowledge, using very simple words and short sentences"
        else
            "a non-technical parent, using clear and simple language"

        val prompt = """Explain the following phone security issue to $audience.
            Keep it reassuring, clear, and action-oriented. Maximum 3 sentences.

            Technical issue: $technicalDetails"""

        val response = geminiApi.generateContent(
            model = "gemini-2.5-flash",
            apiKey = geminiKey(),
            request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(responseMimeType = "text/plain")
            )
        )
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "We found something on your phone that needs your attention. Please follow the recommendations below."
    }
}
