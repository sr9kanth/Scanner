package com.cleanguard.ai.presentation.screenshot

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.cleanguard.ai.domain.model.ConsensusAssessment
import com.cleanguard.ai.presentation.components.ActionButton
import com.cleanguard.ai.presentation.components.SectionCard
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotScreen(
    navController: NavController,
    viewModel: ScreenshotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) viewModel.setImage(bytes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Screenshot Analysis") },
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
            SectionCard(title = "How it works", icon = Icons.Default.AutoAwesome, iconTint = PrimaryBlue) {
                Text(
                    "Upload a screenshot of a suspicious pop-up, notification, or app and our AI will analyze it for scams, malware indicators, and browser hijacking.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (uiState.selectedImageBytes == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = SubtleGray)
                        Spacer(Modifier.height(8.dp))
                        Text("No image selected", color = SubtleGray)
                    }
                }
            } else {
                AsyncImage(
                    model = uiState.selectedImageBytes,
                    contentDescription = "Selected screenshot",
                    modifier = Modifier.fillMaxWidth().height(250.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )
            }

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Choose Screenshot")
            }

            if (uiState.selectedImageBytes != null && !uiState.isAnalyzing) {
                ActionButton(
                    text = "Analyze with AI",
                    onClick = { viewModel.analyze() },
                    icon = Icons.Default.Psychology
                )
            }

            if (uiState.isAnalyzing) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Analyzing with Gemini AI...")
                        Text("Also consulting DeepSeek for a second opinion", style = MaterialTheme.typography.bodySmall, color = SubtleGray)
                    }
                }
            }

            uiState.assessment?.let { assessment ->
                AssessmentResultCard(assessment = assessment)
            }

            uiState.error?.let { error ->
                Card(colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.1f))) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed)
                        Text(error, color = DangerRed)
                    }
                }
            }

            if (uiState.assessment != null || uiState.error != null) {
                OutlinedButton(onClick = { viewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Analyze Another Screenshot")
                }
            }
        }
    }
}

@Composable
fun AssessmentResultCard(assessment: ConsensusAssessment) {
    val riskColor = when {
        assessment.consensusRiskScore >= 80 -> DangerRed
        assessment.consensusRiskScore >= 50 -> SuspiciousOrange
        assessment.consensusRiskScore >= 25 -> ReviewAmber
        else -> SafeGreen
    }

    SectionCard(
        title = "AI Analysis Result",
        icon = Icons.Default.Psychology,
        iconTint = riskColor
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Risk Score:", style = MaterialTheme.typography.bodyMedium)
            Text("${assessment.consensusRiskScore}/100", fontWeight = FontWeight.Bold, color = riskColor)
            Spacer(Modifier.weight(1f))
            Text("Confidence: ${(assessment.consensusConfidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = SubtleGray)
        }

        if (assessment.requiresManualReview) {
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = ReviewAmber.copy(alpha = 0.1f)) {
                Text(
                    "AI models disagree. Manual review recommended.",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ReviewAmber
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("What we found:", fontWeight = FontWeight.SemiBold)
        Text(assessment.plainEnglishSummary, style = MaterialTheme.typography.bodyMedium)

        if (assessment.combinedRecommendations.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("What to do:", fontWeight = FontWeight.SemiBold)
            assessment.combinedRecommendations.forEach { rec ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                    Text("•", color = riskColor)
                    Text(rec, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            assessment.geminiAssessment?.let { Text("Gemini: ${it.riskScore}", style = MaterialTheme.typography.labelSmall, color = SubtleGray) }
            assessment.deepSeekAssessment?.let { Text("| DeepSeek: ${it.riskScore}", style = MaterialTheme.typography.labelSmall, color = SubtleGray) }
        }
    }
}
