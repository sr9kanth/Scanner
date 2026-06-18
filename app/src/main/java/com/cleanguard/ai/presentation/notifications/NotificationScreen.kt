package com.cleanguard.ai.presentation.notifications

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cleanguard.ai.domain.model.NotificationStats
import com.cleanguard.ai.presentation.components.EmptyState
import com.cleanguard.ai.presentation.components.SectionCard
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val spammy by viewModel.spammyApps.collectAsState()
    val all by viewModel.allStats.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Monitor") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = "Notification Spammers", icon = Icons.Default.NotificationsActive, iconTint = ReviewAmber) {
                    if (spammy.isEmpty()) {
                        Text("No notification spammers detected.", style = MaterialTheme.typography.bodyMedium, color = SafeGreen)
                    } else {
                        Text("${spammy.size} apps are sending excessive notifications.", style = MaterialTheme.typography.bodyMedium, color = ReviewAmber)
                    }
                }
            }

            if (all.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.NotificationsOff,
                        title = "No data yet",
                        subtitle = "CleanGuard will track notifications after you grant notification access"
                    )
                }
                item {
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Notification Access")
                    }
                }
            } else {
                items(all, key = { it.packageName }) { stats ->
                    NotificationStatsCard(stats = stats, context = context)
                }
            }
        }
    }
}

@Composable
fun NotificationStatsCard(stats: NotificationStats, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (stats.isSpammy) ReviewAmber.copy(alpha = 0.08f) else CardBackground
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stats.appName, fontWeight = FontWeight.SemiBold)
                    Text(stats.packageName, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                }
                if (stats.isSpammy) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        color = ReviewAmber.copy(alpha = 0.15f)
                    ) {
                        Text("Spammy", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = ReviewAmber, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip(label = "This Week", value = stats.weeklyCount.toString())
                StatChip(label = "Daily Avg", value = "%.1f".format(stats.dailyAverage))
                StatChip(label = "Total", value = stats.totalCount.toString())
            }
            if (stats.isSpammy) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .apply { data = android.net.Uri.parse("package:${stats.packageName}") }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Disable Notifications")
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
    }
}
