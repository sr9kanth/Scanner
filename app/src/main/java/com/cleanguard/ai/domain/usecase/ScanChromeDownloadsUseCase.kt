package com.cleanguard.ai.domain.usecase

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import com.cleanguard.ai.domain.model.ChromeDownloadThreat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ScanChromeDownloadsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend operator fun invoke(): List<ChromeDownloadThreat> = withContext(Dispatchers.IO) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

        val files = downloadsDir?.listFiles()?.filter { file ->
            val name = file.name.lowercase()
            file.isFile && (name.endsWith(".apk") || name.endsWith(".apk.bin"))
        } ?: emptyList()

        files.map { file -> analyze(file) }
    }

    private fun analyze(file: File): ChromeDownloadThreat {
        val pm = context.packageManager

        var label = file.name
        var permissions: Array<String> = emptyArray()

        try {
            val archiveInfo = pm.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.GET_PERMISSIONS
            )
            if (archiveInfo != null) {
                permissions = archiveInfo.requestedPermissions ?: emptyArray()
                archiveInfo.applicationInfo?.let { appInfo ->
                    appInfo.sourceDir = file.absolutePath
                    appInfo.publicSourceDir = file.absolutePath
                    label = pm.getApplicationLabel(appInfo).toString()
                }
            }
        } catch (e: Exception) {
            // Fall back to file name (already set as default)
        }

        var score = 30
        var explanation: String? = null

        if (permissions.contains("android.permission.BIND_ACCESSIBILITY_SERVICE")) {
            score += 40
            explanation = "This installer demands Full Device Accessibility control"
        } else if (permissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            score += 30
            explanation = "This installer demands Full Screen Banner overlays"
        }

        if (explanation == null) {
            val haystack = (file.name + " " + label).lowercase()
            val baits = listOf("booster", "cleaner", "gift", "lucky")
            if (baits.any { haystack.contains(it) }) {
                score += 30
                explanation = "The file name matches known scam-bait patterns"
            }
        }

        if (explanation == null) {
            explanation = "Unknown downloaded package — review before installing."
        }

        return ChromeDownloadThreat(
            fileName = file.name,
            filePath = file.absolutePath,
            fileSizeFormatted = formatSize(file.length()),
            riskScore = score.coerceIn(0, 100),
            explanation = explanation,
            label = label
        )
    }

    private fun formatSize(bytes: Long): String {
        return if (bytes > 1024 * 1024) {
            String.format("%.1f MB", bytes / 1024.0 / 1024.0)
        } else {
            String.format("%.0f KB", bytes / 1024.0)
        }
    }
}
