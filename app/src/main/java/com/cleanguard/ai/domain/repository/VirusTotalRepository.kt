package com.cleanguard.ai.domain.repository

import com.cleanguard.ai.data.remote.dto.VirusTotalStats

interface VirusTotalRepository {
    suspend fun scanByHash(apkPath: String, apiKey: String): Result<VirusTotalStats>
}
