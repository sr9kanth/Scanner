package com.cleanguard.ai.presentation.screenshot

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cleanguard.ai.domain.model.ConsensusAssessment
import com.cleanguard.ai.presentation.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotScreen(
    navController: NavController,
    viewModel: ScreenshotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isGrandparentMode = LocalGrandparentMode.current

    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                previewUri = uri
                previewBitmap = null
                viewModel.setImage(bytes)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Screenshot Check", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight, titleContentColor = OnSurface, navigationIconContentColor = OnSurface)
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Upload a screenshot of a suspicious message, alert, or notification. Our AI will analyze it for scams, phishing attempts, or malware.",
                        fontSize = 13.sp,
                        color = Color(0xFF1A73E8),
                        lineHeight = 20.sp
                    )
                }
            }

            // Image preview area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = BorderStroke(2.dp, color = Color(0xFFCCCCCC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        previewUri != null -> {
                            AsyncImage(
                                model = previewUri,
                                contentDescription = "Selected screenshot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        previewBitmap != null -> {
                            Image(
                                bitmap = previewBitmap!!.asImageBitmap(),
                                contentDescription = "Simulated screenshot",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = SubtleGray,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "No screenshot selected",
                                    fontSize = 13.sp,
                                    color = SubtleGray
                                )
                            }
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choose Screenshot", color = PrimaryBlue, fontSize = 13.sp)
                }
                Button(
                    onClick = { viewModel.analyze(isGrandparentMode) },
                    enabled = uiState.selectedImageBytes != null && !uiState.isAnalyzing,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (uiState.isAnalyzing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze with AI", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // Simulate ad popups button
            OutlinedButton(
                onClick = {
                    val bitmap = createSimulatedPopupBitmap()
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    previewUri = null
                    previewBitmap = bitmap
                    viewModel.setImage(stream.toByteArray())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, ReviewAmber)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = ReviewAmber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Ad Popups", color = ReviewAmber, fontSize = 13.sp)
            }

            // Error message
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = DangerRed,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Results card
            uiState.assessment?.let { assessment ->
                AnalysisResultCard(assessment)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AnalysisResultCard(assessment: ConsensusAssessment) {
    val riskScore = assessment.consensusRiskScore
    val confidencePercent = (assessment.consensusConfidence * 100).toInt()
    val agreementLabel = if (assessment.modelsAgree) "Models Agree" else "Manual Review"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8F5))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Analysis Results",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = agreementLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ReviewAmber
                            )
                        }
                        Text(
                            text = "Confidence: $confidencePercent%",
                            fontSize = 11.sp,
                            color = SubtleGray
                        )
                    }
                }
                // Score gauge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$riskScore",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                        Text(
                            text = "risk",
                            fontSize = 9.sp,
                            color = DangerRed
                        )
                    }
                }
            }

            // Analysis content
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val category = assessment.geminiAssessment?.category
                    ?: assessment.deepSeekAssessment?.category
                if (!category.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DangerRed
                        )
                    }
                }

                Text(
                    text = assessment.plainEnglishSummary,
                    fontSize = 13.sp,
                    color = OnSurface,
                    lineHeight = 20.sp
                )

                if (assessment.combinedRecommendations.isNotEmpty()) {
                    Text(
                        text = "Recommendations:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        assessment.combinedRecommendations.forEach { rec ->
                            BulletPoint(rec)
                        }
                    }
                }

                HorizontalDivider(color = DividerColor)

                val engines = listOfNotNull(
                    assessment.geminiAssessment?.source,
                    assessment.deepSeekAssessment?.source
                )
                if (engines.isNotEmpty()) {
                    Text(
                        text = "Analyzed by ${engines.joinToString(" · ")}",
                        fontSize = 11.sp,
                        color = SubtleGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun createSimulatedPopupBitmap(): Bitmap {
    val bitmap = createBitmap(800, 600)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Background
    paint.color = AndroidColor.WHITE
    canvas.drawRect(0f, 0f, 800f, 600f, paint)

    // Red alert banner
    paint.color = AndroidColor.RED
    canvas.drawRect(100f, 150f, 700f, 250f, paint)

    // Inner popup body
    paint.color = AndroidColor.rgb(255, 235, 238)
    canvas.drawRect(100f, 250f, 700f, 450f, paint)

    // "Tap to fix" button
    paint.color = AndroidColor.RED
    canvas.drawRect(250f, 370f, 550f, 430f, paint)

    return bitmap
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("•", fontSize = 13.sp, color = DangerRed, fontWeight = FontWeight.Bold)
        Text(text, fontSize = 13.sp, color = OnSurface, lineHeight = 19.sp)
    }
}
