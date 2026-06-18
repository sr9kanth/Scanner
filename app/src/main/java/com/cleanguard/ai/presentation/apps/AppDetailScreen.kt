package com.cleanguard.ai.presentation.apps

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.presentation.components.ActionButton
import com.cleanguard.ai.presentation.components.RiskBadge
import com.cleanguard.ai.presentation.components.SectionCard
import com.cleanguard.ai.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String,
    navController: NavController,
    viewModel: AppListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val app = uiState.apps.find { it.packageName == packageName }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.appName ?: "App Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (app == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (app.riskLevel) {
                        RiskLevel.SAFE -> SafeGreen.copy(alpha = 0.1f)
                        RiskLevel.REVIEW -> ReviewAmber.copy(alpha = 0.1f)
                        RiskLevel.SUSPICIOUS -> SuspiciousOrange.copy(alpha = 0.1f)
                        RiskLevel.REMOVE_IMMEDIATELY -> DangerRed.copy(alpha = 0.1f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(app.riskLevel.parentFriendlyLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    RiskBadge(riskLevel = app.riskLevel)
                    Text("Risk Score: ${app.riskScore}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (app.hasAccessibilityService) {
                WarnCard(
                    title = "Screen Access Detected",
                    message = "This app can see what happens on your screen and control parts of your phone. Unless you trust this app completely, we recommend turning this off."
                )
            }
            if (app.hasOverlayPermission) {
                WarnCard(
                    title = "Pop-up Permission",
                    message = "This app can display pop-up windows over other apps. This is sometimes used by scam apps to show fake alerts."
                )
            }

            SectionCard(title = "App Details", icon = Icons.Default.Info) {
                DetailRow("Package", app.packageName)
                DetailRow("Version", app.versionName)
                DetailRow("Installed", SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(app.installDate)))
                DetailRow("Updated", SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(app.lastUpdateDate)))
                DetailRow("Source", app.installerPackage?.let {
                    if (it == "com.android.vending") "Google Play Store" else it
                } ?: "Unknown / Sideloaded")
                DetailRow("Permissions", "${app.permissions.size} permissions")
            }

            if (app.riskLevel != RiskLevel.SAFE) {
                ActionButton(
                    text = "Uninstall App",
                    onClick = {
                        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
                        context.startActivity(intent)
                    },
                    isDestructive = app.riskLevel == RiskLevel.REMOVE_IMMEDIATELY,
                    icon = Icons.Default.Delete
                )
            }
        }
    }
}

@Composable
fun WarnCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ReviewAmber.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = ReviewAmber)
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, color = ReviewAmber)
                Text(message, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SubtleGray, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
    }
}
