package com.cleanguard.ai.presentation.accessibility

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.presentation.components.ActionButton
import com.cleanguard.ai.presentation.components.EmptyState
import com.cleanguard.ai.presentation.components.SectionCard
import com.cleanguard.ai.presentation.theme.ReviewAmber
import com.cleanguard.ai.presentation.theme.SafeGreen
import com.cleanguard.ai.presentation.theme.SubtleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    navController: NavController,
    viewModel: AccessibilityViewModel = hiltViewModel()
) {
    val apps by viewModel.accessibilityApps.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility Services") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                SectionCard(title = "What is this?", icon = Icons.Default.Info, iconTint = MaterialTheme.colorScheme.primary) {
                    Text(
                        "Accessibility services can see everything happening on your screen and control your phone. They are useful for some apps (like screen readers), but scam apps sometimes use them to steal information.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (apps.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "No Risks Found",
                        subtitle = "No suspicious apps are using accessibility services"
                    )
                }
            } else {
                item {
                    Text(
                        "${apps.size} app(s) with accessibility access",
                        style = MaterialTheme.typography.titleSmall,
                        color = ReviewAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(apps, key = { it.packageName }) { app ->
                    AccessibilityAppCard(app = app)
                }
                item {
                    ActionButton(
                        text = "Open Accessibility Settings",
                        onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                        icon = Icons.Default.Settings
                    )
                }
            }
        }
    }
}

@Composable
fun AccessibilityAppCard(app: AppInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ReviewAmber.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Accessibility, contentDescription = null, tint = ReviewAmber, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, fontWeight = FontWeight.SemiBold)
                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                Spacer(Modifier.height(4.dp))
                Text(
                    "This app can see what happens on your screen and control parts of your phone. Unless you trust this app completely, we recommend turning this off.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
