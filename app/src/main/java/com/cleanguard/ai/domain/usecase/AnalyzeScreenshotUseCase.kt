package com.cleanguard.ai.domain.usecase

import com.cleanguard.ai.domain.model.AIThreatAssessment
import com.cleanguard.ai.domain.model.ConsensusAssessment
import com.cleanguard.ai.domain.repository.AIRepository
import com.cleanguard.ai.engine.AIConsensusEngine
import javax.inject.Inject

class AnalyzeScreenshotUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val consensusEngine: AIConsensusEngine
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        isGrandparentMode: Boolean = false
    ): Result<ConsensusAssessment> = runCatching {
        val screenshotPrompt = """
            Analyze this Android screenshot for security threats.
            Identify: malware indicators, scam indicators, adware, fake virus warnings,
            browser hijacking, notification abuse, suspicious apps.
            Return JSON: {"risk_score": 0-100, "confidence": 0.0-1.0, "category": "string",
            "offending_app": "string or null", "recommended_actions": ["string"],
            "reasoning": "string", "explanation": "string"}
        """.trimIndent()

        val geminiResult = aiRepository.analyzeWithGemini(screenshotPrompt, imageBytes)
        val geminiAssessment = geminiResult.getOrThrow()

        val deepSeekResult = aiRepository.validateWithDeepSeek(screenshotPrompt, geminiAssessment)
        val deepSeekAssessment = deepSeekResult.getOrNull()

        val consensus = consensusEngine.buildConsensus(geminiAssessment, deepSeekAssessment)

        val plainSummary = aiRepository.generatePlainEnglishExplanation(
            technicalDetails = "Risk score: ${consensus.consensusRiskScore}. Category: ${geminiAssessment.category}. ${geminiAssessment.explanation}",
            isGrandparentMode = isGrandparentMode
        ).getOrElse { geminiAssessment.explanation }

        consensus.copy(plainEnglishSummary = plainSummary)
    }
}
