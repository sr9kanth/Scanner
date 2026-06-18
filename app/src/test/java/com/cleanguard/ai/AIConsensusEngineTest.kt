package com.cleanguard.ai

import com.cleanguard.ai.domain.model.AIThreatAssessment
import com.cleanguard.ai.engine.AIConsensusEngine
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class AIConsensusEngineTest {

    private lateinit var engine: AIConsensusEngine

    @Before
    fun setUp() {
        engine = AIConsensusEngine()
    }

    @Test
    fun `models agreeing produces high confidence`() {
        val gemini = assessment("Gemini", riskScore = 75, confidence = 0.9)
        val deepSeek = assessment("DeepSeek", riskScore = 80, confidence = 0.85)

        val consensus = engine.buildConsensus(gemini, deepSeek)

        assertThat(consensus.modelsAgree).isTrue()
        assertThat(consensus.consensusRiskScore).isIn(70..82)
        assertThat(consensus.requiresManualReview).isFalse()
    }

    @Test
    fun `models disagreeing by more than 40 triggers manual review`() {
        val gemini = assessment("Gemini", riskScore = 20, confidence = 0.8)
        val deepSeek = assessment("DeepSeek", riskScore = 85, confidence = 0.9)

        val consensus = engine.buildConsensus(gemini, deepSeek)

        assertThat(consensus.modelsAgree).isFalse()
        assertThat(consensus.requiresManualReview).isTrue()
    }

    @Test
    fun `single model (no deepseek) produces valid consensus`() {
        val gemini = assessment("Gemini", riskScore = 60, confidence = 0.75)

        val consensus = engine.buildConsensus(gemini, null)

        assertThat(consensus.consensusRiskScore).isEqualTo(60)
        assertThat(consensus.modelsAgree).isTrue()
        assertThat(consensus.deepSeekAssessment).isNull()
    }

    @Test
    fun `combined recommendations are deduplicated`() {
        val rec = "Uninstall immediately"
        val gemini = assessment("Gemini", riskScore = 90, confidence = 0.9, recommendations = listOf(rec, "Check permissions"))
        val deepSeek = assessment("DeepSeek", riskScore = 85, confidence = 0.85, recommendations = listOf(rec, "Factory reset"))

        val consensus = engine.buildConsensus(gemini, deepSeek)

        assertThat(consensus.combinedRecommendations).containsNoDuplicates()
        assertThat(consensus.combinedRecommendations).contains(rec)
    }

    private fun assessment(
        source: String,
        riskScore: Int,
        confidence: Double,
        recommendations: List<String> = listOf("Action needed")
    ) = AIThreatAssessment(
        source = source,
        riskScore = riskScore,
        confidence = confidence,
        category = "test",
        explanation = "Test explanation",
        recommendations = recommendations
    )
}
