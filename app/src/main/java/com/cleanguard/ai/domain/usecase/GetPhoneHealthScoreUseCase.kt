package com.cleanguard.ai.domain.usecase

import com.cleanguard.ai.domain.model.PhoneHealthScore
import com.cleanguard.ai.domain.repository.AppScanRepository
import com.cleanguard.ai.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPhoneHealthScoreUseCase @Inject constructor(
    private val appScanRepository: AppScanRepository,
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<PhoneHealthScore> = combine(
        appScanRepository.getHighRiskApps(),
        appScanRepository.getAccessibilityApps(),
        appScanRepository.getOverlayApps(),
        notificationRepository.getSpammyApps()
    ) { highRisk, accessibility, overlay, spammy ->
        PhoneHealthScore.calculate(
            highRiskApps = highRisk.count { it.riskScore >= 100 },
            notificationAbusers = spammy.size,
            accessibilityRisks = accessibility.size,
            overlayRisks = overlay.size,
            privacyRisks = highRisk.count { it.permissions.size > 20 },
            lastScanTimestamp = null
        )
    }
}
