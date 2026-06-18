package com.cleanguard.ai

import com.cleanguard.ai.data.local.dao.ThreatIntelDao
import com.cleanguard.ai.data.local.entities.*
import com.cleanguard.ai.engine.RiskScoringEngine
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RiskScoringEngineTest {

    private lateinit var threatIntelDao: ThreatIntelDao
    private lateinit var engine: RiskScoringEngine

    @Before
    fun setUp() {
        threatIntelDao = mock()
        engine = RiskScoringEngine(threatIntelDao)
    }

    @Test
    fun `clean app scores zero`() = runTest {
        stubNoThreats("com.example.cleanapp")
        val score = engine.calculateScore(
            permissions = emptyList(),
            installSource = "com.android.vending",
            installDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
            hasAccessibility = false,
            hasOverlay = false,
            hasNotification = false,
            packageName = "com.example.cleanapp"
        )
        assertThat(score).isEqualTo(0)
    }

    @Test
    fun `accessibility service adds 40 points`() = runTest {
        stubNoThreats("com.example.app")
        val score = engine.calculateScore(
            permissions = emptyList(),
            installSource = "com.android.vending",
            installDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
            hasAccessibility = true,
            hasOverlay = false,
            hasNotification = false,
            packageName = "com.example.app"
        )
        assertThat(score).isEqualTo(40)
    }

    @Test
    fun `overlay permission adds 30 points`() = runTest {
        stubNoThreats("com.example.app")
        val score = engine.calculateScore(
            permissions = emptyList(),
            installSource = "com.android.vending",
            installDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
            hasAccessibility = false,
            hasOverlay = true,
            hasNotification = false,
            packageName = "com.example.app"
        )
        assertThat(score).isEqualTo(30)
    }

    @Test
    fun `unknown installer adds 30 points`() = runTest {
        stubNoThreats("com.example.app")
        val score = engine.calculateScore(
            permissions = emptyList(),
            installSource = null,
            installDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
            hasAccessibility = false,
            hasOverlay = false,
            hasNotification = false,
            packageName = "com.example.app"
        )
        assertThat(score).isEqualTo(30)
    }

    @Test
    fun `known scam app adds 100 points`() = runTest {
        whenever(threatIntelDao.getAdware("com.scam.app")).thenReturn(null)
        whenever(threatIntelDao.getScamApp("com.scam.app")).thenReturn(
            KnownScamAppEntity("com.scam.app", "Scam App", "phishing", "Known phishing app")
        )
        whenever(threatIntelDao.getFakeCleaner("com.scam.app")).thenReturn(null)
        whenever(threatIntelDao.getFakeAntivirus("com.scam.app")).thenReturn(null)
        whenever(threatIntelDao.getBrowserHijacker("com.scam.app")).thenReturn(null)
        whenever(threatIntelDao.getNotificationAbuser("com.scam.app")).thenReturn(null)

        val score = engine.calculateScore(
            permissions = emptyList(),
            installSource = "com.android.vending",
            installDate = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
            hasAccessibility = false,
            hasOverlay = false,
            hasNotification = false,
            packageName = "com.scam.app"
        )
        assertThat(score).isAtLeast(100)
    }

    @Test
    fun `recently installed app adds 15 points`() = runTest {
        stubNoThreats("com.example.app")
        val recentInstall = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000)
        val score = engine.calculateScore(
            permissions = emptyList(),
            installSource = "com.android.vending",
            installDate = recentInstall,
            hasAccessibility = false,
            hasOverlay = false,
            hasNotification = false,
            packageName = "com.example.app"
        )
        assertThat(score).isEqualTo(15)
    }

    private suspend fun stubNoThreats(pkg: String) {
        whenever(threatIntelDao.getAdware(pkg)).thenReturn(null)
        whenever(threatIntelDao.getScamApp(pkg)).thenReturn(null)
        whenever(threatIntelDao.getFakeCleaner(pkg)).thenReturn(null)
        whenever(threatIntelDao.getFakeAntivirus(pkg)).thenReturn(null)
        whenever(threatIntelDao.getBrowserHijacker(pkg)).thenReturn(null)
        whenever(threatIntelDao.getNotificationAbuser(pkg)).thenReturn(null)
    }
}
