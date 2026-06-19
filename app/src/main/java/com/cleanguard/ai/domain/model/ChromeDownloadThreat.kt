package com.cleanguard.ai.domain.model

data class ChromeDownloadThreat(
    val fileName: String,
    val filePath: String,
    val fileSizeFormatted: String,
    val riskScore: Int,
    val explanation: String,
    val label: String
)
