package com.cleanguard.ai.domain.repository

import com.cleanguard.ai.domain.model.AIThreatAssessment

interface AIRepository {
    suspend fun analyzeWithGemini(prompt: String, imageBytes: ByteArray?): Result<AIThreatAssessment>
    suspend fun validateWithDeepSeek(originalPrompt: String, geminiAssessment: AIThreatAssessment): Result<AIThreatAssessment>
    suspend fun generatePlainEnglishExplanation(technicalDetails: String, isGrandparentMode: Boolean): Result<String>
}
