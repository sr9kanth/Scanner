package com.cleanguard.ai.engine

import com.cleanguard.ai.domain.model.AIThreatAssessment
import com.cleanguard.ai.domain.model.ConsensusAssessment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIConsensusEngine @Inject constructor() {

    companion object {
        private const val DISAGREEMENT_THRESHOLD = 40
    }

    fun buildConsensus(
        gemini: AIThreatAssessment,
        deepSeek: AIThreatAssessment?
    ): ConsensusAssessment {
        if (deepSeek == null) {
            return ConsensusAssessment(
                geminiAssessment = gemini,
                deepSeekAssessment = null,
                consensusRiskScore = gemini.riskScore,
                consensusConfidence = gemini.confidence * 0.8,
                modelsAgree = true,
                combinedRecommendations = gemini.recommendations,
                plainEnglishSummary = gemini.explanation
            )
        }

        val scoreDiff = kotlin.math.abs(gemini.riskScore - deepSeek.riskScore)
        val modelsAgree = scoreDiff <= DISAGREEMENT_THRESHOLD

        val consensusScore = if (modelsAgree) {
            ((gemini.riskScore * gemini.confidence + deepSeek.riskScore * deepSeek.confidence) /
                    (gemini.confidence + deepSeek.confidence)).toInt()
        } else {
            maxOf(gemini.riskScore, deepSeek.riskScore)
        }

        val consensusConfidence = if (modelsAgree) {
            (gemini.confidence + deepSeek.confidence) / 2
        } else {
            minOf(gemini.confidence, deepSeek.confidence) * 0.6
        }

        val combined = (gemini.recommendations + deepSeek.recommendations).distinct()

        return ConsensusAssessment(
            geminiAssessment = gemini,
            deepSeekAssessment = deepSeek,
            consensusRiskScore = consensusScore,
            consensusConfidence = consensusConfidence,
            modelsAgree = modelsAgree,
            combinedRecommendations = combined,
            plainEnglishSummary = gemini.explanation
        )
    }
}
