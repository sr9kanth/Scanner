package com.cleanguard.ai.presentation.chrome

import android.content.Intent
import android.net.Uri
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
import com.cleanguard.ai.presentation.components.SectionCard
import com.cleanguard.ai.presentation.theme.PrimaryBlue
import com.cleanguard.ai.presentation.theme.ReviewAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromeScreen(
    navController: NavController,
    viewModel: ChromeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cleanupSteps = listOf(
        CleanupStep("Remove Notification Permissions", "Many websites trick you into allowing notifications that send spam. We'll help you remove them.", "Open Chrome Settings > Notifications > Remove suspicious sites"),
        CleanupStep("Clear Browsing Data", "Clearing your browsing history, cookies, and cache can remove adware that's been saved in your browser.", "Open Chrome Settings > Privacy and Security > Clear browsing data"),
        CleanupStep("Disable Pop-ups", "Make sure pop-ups are blocked so scam websites can't show you fake alerts.", "Open Chrome Settings > Site Settings > Pop-ups and redirects > Blocked"),
        CleanupStep("Reset Chrome Settings", "If nothing else works, resetting Chrome to its factory settings will remove most browser-based problems.", "Open Chrome Settings > Advanced > Reset settings")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chrome Cleanup") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            SectionCard(title = "Chrome Status", icon = Icons.Default.Language) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        if (uiState.isChromeInstalled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (uiState.isChromeInstalled) com.cleanguard.ai.presentation.theme.SafeGreen else ReviewAmber
                    )
                    Text(if (uiState.isChromeInstalled) "Google Chrome is installed" else "Chrome not found")
                }
            }

            Text("Cleanup Guide", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Follow these steps to fix browser-related popup and notification problems. Tap each step to open the right settings page.",
                style = MaterialTheme.typography.bodyMedium
            )

            cleanupSteps.forEachIndexed { index, step ->
                CleanupStepCard(
                    step = step,
                    stepNumber = index + 1,
                    isComplete = uiState.currentStep > index,
                    onClick = {
                        viewModel.setStep(index + 1)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://support.google.com/chrome/"))
                        context.startActivity(intent)
                    }
                )
            }

            Button(
                onClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage("com.android.chrome")
                    if (intent != null) context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Chrome")
            }
        }
    }
}

data class CleanupStep(val title: String, val description: String, val instruction: String)

@Composable
fun CleanupStepCard(step: CleanupStep, stepNumber: Int, isComplete: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete) com.cleanguard.ai.presentation.theme.SafeGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isComplete) com.cleanguard.ai.presentation.theme.SafeGreen else PrimaryBlue,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isComplete) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = androidx.compose.ui.graphics.Color.White)
                    } else {
                        Text("$stepNumber", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(step.title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(step.description, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text(step.instruction, style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
            }
        }
    }
}
