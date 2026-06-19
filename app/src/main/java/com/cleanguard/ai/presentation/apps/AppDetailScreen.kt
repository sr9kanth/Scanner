package com.cleanguard.ai.presentation.apps

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.domain.model.RiskSignal
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    navController: NavController,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val app = uiState.app
    val appName = app?.appName ?: packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }

    fun openUninstall() {
        context.startActivity(Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
        })
    }

    fun openAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun openAppSettings() {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appName, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { openUninstall() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uninstall App",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val riskLevel = app?.riskLevel ?: RiskLevel.REVIEW
            val riskColor = riskColorFor(riskLevel)

            // Risk summary banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.08f)),
                border = BorderStroke(width = 2.dp, color = riskColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.18f))
                    ) {
                        Icon(
                            imageVector = if (riskLevel == RiskLevel.SAFE) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = riskLevel.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = riskLevel.parentFriendlyLabel,
                            fontSize = 12.sp,
                            color = SubtleGray
                        )
                    }
                    if (app != null) {
                        Text(
                            text = "${app.riskScore}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }
            }

            // Signals Detected (expandable)
            SignalsCard(signals = uiState.signals, isLoading = uiState.isLoading)

            // AI Security Check
            AiSecurityCard(
                isQuerying = uiState.isQueryingAi,
                result = uiState.aiResult,
                error = uiState.aiError,
                onQuery = { viewModel.queryAi() }
            )

            // Accessibility permission card (only when the app actually holds it)
            if (app?.hasAccessibilityService == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(2.dp, SuspiciousOrange.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Screen Access Permission",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "This app has accessibility / screen access that lets it read and interact with other apps. This is a high-risk permission.",
                            fontSize = 13.sp,
                            color = SubtleGray,
                            lineHeight = 20.sp
                        )
                        OutlinedButton(
                            onClick = { openAccessibilitySettings() },
                            border = BorderStroke(1.5.dp, SuspiciousOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Turn Off Screen Access",
                                color = SuspiciousOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // App Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "App Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )
                    HorizontalDivider(color = DividerColor)
                    AppDetailRow("Package", packageName)
                    if (app != null) {
                        AppDetailRow("Version", app.versionName.ifBlank { "—" })
                        AppDetailRow("Size", formatSize(app.sizeBytes))
                        AppDetailRow("Installer", app.installerPackage ?: "Unknown / side-loaded")
                        AppDetailRow("Permissions", "${app.permissions.size}")
                        AppDetailRow("System app", if (app.isSystemApp) "Yes" else "No")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { openAppSettings() },
                        border = BorderStroke(1.dp, PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open App Settings", color = PrimaryBlue, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SignalsCard(signals: List<RiskSignal>, isLoading: Boolean) {
    var expanded by remember { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Signals Detected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = SubtleGray
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = DividerColor)
                    when {
                        isLoading -> Text("Loading signals…", fontSize = 13.sp, color = SubtleGray)
                        signals.isEmpty() -> Text(
                            "No risk signals detected for this app.",
                            fontSize = 13.sp,
                            color = SubtleGray
                        )
                        else -> signals.forEach { signal ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = signal.label,
                                    fontSize = 13.sp,
                                    color = OnSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (signal.points > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DangerRed.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+${signal.points}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DangerRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiSecurityCard(
    isQuerying: Boolean,
    result: com.cleanguard.ai.domain.model.AIThreatAssessment?,
    error: String?,
    onQuery: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Security Check",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
            }

            when {
                isQuerying -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Asking AI to review this app…", fontSize = 13.sp, color = SubtleGray)
                }

                result != null -> {
                    HorizontalDivider(color = DividerColor)
                    Text(
                        text = "${result.source} · ${result.category}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Risk score: ", fontSize = 13.sp, color = SubtleGray)
                        Text(
                            text = "${result.riskScore}/100",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = aiScoreColor(result.riskScore)
                        )
                    }
                    Text(text = result.explanation, fontSize = 13.sp, color = OnSurface, lineHeight = 20.sp)
                    if (result.recommendations.isNotEmpty()) {
                        Text(
                            "Recommendations",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        result.recommendations.forEach { rec ->
                            Row {
                                Text("•  ", fontSize = 13.sp, color = SubtleGray)
                                Text(rec, fontSize = 13.sp, color = SubtleGray, lineHeight = 19.sp)
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onQuery,
                        border = BorderStroke(1.dp, PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Re-run AI Check", color = PrimaryBlue, fontSize = 13.sp)
                    }
                }

                else -> {
                    if (error != null) {
                        Text(text = error, fontSize = 13.sp, color = DangerRed, lineHeight = 19.sp)
                    } else {
                        Text(
                            text = "Run an AI security review to get a second opinion on this app's behaviour.",
                            fontSize = 13.sp,
                            color = SubtleGray,
                            lineHeight = 20.sp
                        )
                    }
                    Button(
                        onClick = onQuery,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (error != null) "Try Again" else "Query AI", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = SubtleGray)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = OnSurface,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

private fun riskColorFor(level: RiskLevel): Color = when (level) {
    RiskLevel.SAFE -> SafeGreen
    RiskLevel.REVIEW -> ReviewAmber
    RiskLevel.SUSPICIOUS -> SuspiciousOrange
    RiskLevel.REMOVE_IMMEDIATELY -> DangerRed
}

private fun aiScoreColor(score: Int): Color = when {
    score >= 70 -> DangerRed
    score >= 40 -> SuspiciousOrange
    else -> SafeGreen
}

private fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> "—"
    bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
    else -> String.format("%.0f KB", bytes / 1024.0)
}
