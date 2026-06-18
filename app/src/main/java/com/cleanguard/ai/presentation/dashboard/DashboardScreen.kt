package com.cleanguard.ai.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.HealthGrade
import com.cleanguard.ai.presentation.components.*
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CleanGuard AI", fontWeight = FontWeight.Bold)
                        Text("AI Phone Health Checkup", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.startScan() },
                icon = { Icon(Icons.Default.Search, contentDescription = null) },
                text = { Text("Scan Now") },
                containerColor = MaterialTheme.colorScheme.primary
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
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        color = DangerRed
                    )
                }
            }

            // Health Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Phone Health Score", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(20.dp))

                    val score = uiState.healthScore?.score ?: 0
                    val grade = uiState.healthScore?.grade ?: HealthGrade.AT_RISK

                    HealthScoreRing(score = score, grade = grade, size = 160)

                    Spacer(Modifier.height(12.dp))
                    uiState.lastScanTimestamp?.let { ts ->
                        Text(
                            text = "Last scan: ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } ?: Text(
                        text = "Tap 'Scan Now' for your first scan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats Grid
            uiState.healthScore?.let { health ->
                Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        count = health.highRiskApps,
                        label = "High Risk\nApps",
                        color = DangerRed,
                        icon = Icons.Default.BugReport,
                        onClick = { navController.navigate(Screen.AppList.route) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = health.accessibilityRisks,
                        label = "Accessibility\nRisks",
                        color = SuspiciousOrange,
                        icon = Icons.Default.Accessibility,
                        onClick = { navController.navigate(Screen.Accessibility.route) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = health.overlayRisks,
                        label = "Overlay\nRisks",
                        color = ReviewAmber,
                        icon = Icons.Default.Layers,
                        onClick = { navController.navigate(Screen.Overlay.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        count = health.notificationAbusers,
                        label = "Notification\nSpammers",
                        color = ReviewAmber,
                        icon = Icons.Default.NotificationsActive,
                        onClick = { navController.navigate(Screen.Notifications.route) },
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        count = health.privacyRisks,
                        label = "Privacy\nRisks",
                        color = PrimaryBlue,
                        icon = Icons.Default.Shield,
                        onClick = { navController.navigate(Screen.AppList.route) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.weight(1f))
                }
            }

            // Quick Actions
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickActionRow(
                    icon = Icons.Default.PhotoCamera,
                    title = "Analyze a Screenshot",
                    subtitle = "Upload a screenshot for AI analysis",
                    onClick = { navController.navigate(Screen.Screenshot.route) }
                )
                QuickActionRow(
                    icon = Icons.Default.Language,
                    title = "Chrome Cleanup",
                    subtitle = "Fix browser popups and notification abuse",
                    onClick = { navController.navigate(Screen.Chrome.route) }
                )
                QuickActionRow(
                    icon = Icons.Default.Assessment,
                    title = "View Full Report",
                    subtitle = "Export detailed health report",
                    onClick = { navController.navigate(Screen.Report.route) }
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun QuickActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SubtleGray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SubtleGray)
        }
    }
}
