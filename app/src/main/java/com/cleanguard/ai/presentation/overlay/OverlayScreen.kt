package com.cleanguard.ai.presentation.overlay

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
import com.cleanguard.ai.presentation.theme.SubtleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayScreen(
    navController: NavController,
    viewModel: OverlayViewModel = hiltViewModel()
) {
    val apps by viewModel.overlayApps.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pop-up Permissions") },
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
                SectionCard(title = "What are pop-up permissions?", icon = Icons.Default.Info) {
                    Text(
                        "Some apps have permission to show pop-ups and windows over other apps. Scam apps use this to show fake virus alerts and trick you into calling a fake support number.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (apps.isEmpty()) {
                item { EmptyState(icon = Icons.Default.CheckCircle, title = "No Pop-up Risks", subtitle = "No suspicious apps have pop-up permissions") }
            } else {
                items(apps) { app ->
                    OverlayAppCard(app = app)
                }
                item {
                    ActionButton(
                        text = "Manage Pop-up Permissions",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                        },
                        icon = Icons.Default.Settings
                    )
                }
            }
        }
    }
}

@Composable
fun OverlayAppCard(app: AppInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ReviewAmber.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(app.appName, fontWeight = FontWeight.SemiBold)
            Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
            Text(
                "This app can display pop-up windows over other apps. If you see unexpected pop-ups or fake virus alerts, this app might be the cause.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
