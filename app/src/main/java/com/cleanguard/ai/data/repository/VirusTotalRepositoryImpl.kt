package com.cleanguard.ai.data.repository

import com.cleanguard.ai.data.remote.api.VirusTotalApiService
import com.cleanguard.ai.data.remote.dto.VirusTotalStats
import com.cleanguard.ai.domain.repository.VirusTotalRepository
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirusTotalRepositoryImpl @Inject constructor(
    private val virusTotalApi: VirusTotalApiService
) : VirusTotalRepository {

    override suspend fun scanByHash(apkPath: String, apiKey: String): Result<VirusTotalStats> = runCatching {
        val hash = sha256(apkPath)
        val response = virusTotalApi.getFileReport(hash, apiKey)
        response.data?.attributes?.lastAnalysisStats
            ?: throw IllegalStateException(response.error?.message ?: "No data in VT response")
    }

    private fun sha256(filePath: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        File(filePath).inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
