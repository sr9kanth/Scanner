package com.cleanguard.ai.presentation.scanner

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.presentation.components.ActionButton
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Scan") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            when (uiState.phase) {
                ScanPhase.IDLE -> {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Ready to Scan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "We'll check your installed apps for potential risks, suspicious permissions, and threats.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubtleGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    ActionButton(text = "Start Scan", onClick = { viewModel.startScan() }, icon = Icons.Default.Search)
                }

                ScanPhase.SCANNING_APPS, ScanPhase.CHECKING_THREATS, ScanPhase.ANALYZING -> {
                    CircularProgressIndicator(modifier = Modifier.size(80.dp), strokeWidth = 6.dp)
                    Text(
                        text = when (uiState.phase) {
                            ScanPhase.SCANNING_APPS -> "Scanning installed apps..."
                            ScanPhase.CHECKING_THREATS -> "Checking threat database..."
                            else -> "Analyzing risks..."
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    LinearProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.currentApp.isNotEmpty()) {
                        Text(uiState.currentApp, style = MaterialTheme.typography.bodySmall, color = SubtleGray)
                    }
                }

                ScanPhase.COMPLETE -> {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = SafeGreen
                    )
                    Text("Scan Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    val highRisk = uiState.scannedApps.count { it.riskLevel == RiskLevel.REMOVE_IMMEDIATELY }
                    val suspicious = uiState.scannedApps.count { it.riskLevel == RiskLevel.SUSPICIOUS }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (highRisk > 0) DangerRed.copy(0.1f) else SafeGreen.copy(0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${uiState.scannedApps.size} apps scanned", fontWeight = FontWeight.Medium)
                            if (highRisk > 0) Text("$highRisk apps need immediate removal", color = DangerRed, fontWeight = FontWeight.SemiBold)
                            if (suspicious > 0) Text("$suspicious suspicious apps found", color = SuspiciousOrange)
                            if (highRisk == 0 && suspicious == 0) Text("No major threats found", color = SafeGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    ActionButton(text = "View Results", onClick = { navController.navigate(Screen.AppList.route) })
                    OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Scan Again")
                    }
                }
            }

            uiState.error?.let {
                Text(text = it, color = DangerRed, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
