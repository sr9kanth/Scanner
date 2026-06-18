package com.cleanguard.ai.domain.usecase

import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.repository.AppScanRepository
import javax.inject.Inject

class ScanAppsUseCase @Inject constructor(
    private val appScanRepository: AppScanRepository
) {
    suspend operator fun invoke(): Result<List<AppInfo>> = runCatching {
        appScanRepository.scanInstalledApps()
    }
}
