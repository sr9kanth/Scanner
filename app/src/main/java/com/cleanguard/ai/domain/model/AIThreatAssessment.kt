package com.cleanguard.ai.domain.model

data class AIThreatAssessment(
    val source: String,
    val riskScore: Int,
    val confidence: Double,
    val category: String,
    val explanation: String,
    val recommendations: List<String>,
    val reasoning: String = ""
)

data class ConsensusAssessment(
    val geminiAssessment: AIThreatAssessment?,
    val deepSeekAssessment: AIThreatAssessment?,
    val consensusRiskScore: Int,
    val consensusConfidence: Double,
    val modelsAgree: Boolean,
    val combinedRecommendations: List<String>,
    val plainEnglishSummary: String
) {
    val requiresManualReview: Boolean
        get() = !modelsAgree
}
