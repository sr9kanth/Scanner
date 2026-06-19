package com.cleanguard.ai.presentation.report

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.HealthGrade
import com.cleanguard.ai.presentation.components.HealthScoreRing
import com.cleanguard.ai.presentation.components.SectionCard
import com.cleanguard.ai.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Report") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportText = viewModel.buildReportText()
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "CleanGuard AI Security Diagnostic")
                            putExtra(Intent.EXTRA_TEXT, reportText)
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "Share Health Diagnostic")
                        )
                    }) {
                        Icon(Icons.Default.Share, "Share Health Diagnostic")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Generated: ${SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = SubtleGray
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val score = uiState.healthScore?.score ?: 0
                    val grade = uiState.healthScore?.grade ?: HealthGrade.AT_RISK
                    HealthScoreRing(score = score, grade = grade, size = 120)
                    Spacer(Modifier.height(12.dp))
                    Text("Overall Phone Health", style = MaterialTheme.typography.titleMedium)
                }
            }

            SectionCard(title = "High Risk Apps", icon = Icons.Default.BugReport, iconTint = DangerRed) {
                if (uiState.highRiskApps.isEmpty()) {
                    Text("No high risk apps found.", color = SafeGreen)
                } else {
                    uiState.highRiskApps.forEach { app ->
                        ReportRow(name = app.appName, detail = "Risk: ${app.riskScore}  •  ${app.riskLevel.label}", color = DangerRed)
                    }
                }
            }

            SectionCard(title = "Accessibility Risks", icon = Icons.Default.Accessibility, iconTint = SuspiciousOrange) {
                if (uiState.accessibilityApps.isEmpty()) {
                    Text("No accessibility service risks.", color = SafeGreen)
                } else {
                    uiState.accessibilityApps.forEach { app ->
                        ReportRow(name = app.appName, detail = "Has screen access permission", color = SuspiciousOrange)
                    }
                }
            }

            SectionCard(title = "Pop-up Permission Risks", icon = Icons.Default.Layers, iconTint = ReviewAmber) {
                if (uiState.overlayApps.isEmpty()) {
                    Text("No overlay permission risks.", color = SafeGreen)
                } else {
                    uiState.overlayApps.forEach { app ->
                        ReportRow(name = app.appName, detail = "Can show pop-ups over other apps", color = ReviewAmber)
                    }
                }
            }

            SectionCard(title = "Notification Spammers", icon = Icons.Default.NotificationsActive, iconTint = ReviewAmber) {
                if (uiState.notificationAbusers.isEmpty()) {
                    Text("No notification spammers detected.", color = SafeGreen)
                } else {
                    uiState.notificationAbusers.forEach { stats ->
                        ReportRow(name = stats.appName, detail = "${stats.weeklyCount} notifications this week", color = ReviewAmber)
                    }
                }
            }

            SectionCard(title = "Recommended Actions", icon = Icons.Default.PlaylistAddCheck) {
                val actions = buildList {
                    if (uiState.highRiskApps.isNotEmpty()) add("Uninstall ${uiState.highRiskApps.size} high-risk app(s)")
                    if (uiState.accessibilityApps.isNotEmpty()) add("Review accessibility service permissions")
                    if (uiState.overlayApps.isNotEmpty()) add("Review pop-up permissions")
                    if (uiState.notificationAbusers.isNotEmpty()) add("Disable notifications for spammy apps")
                    if (isEmpty()) add("Your phone is in good shape! Keep running regular scans.")
                }
                actions.forEach { action ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.RadioButtonChecked, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(action, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ReportRow(name: String, detail: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = color, modifier = Modifier.size(8.dp)) {}
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(detail, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
        }
    }
}
