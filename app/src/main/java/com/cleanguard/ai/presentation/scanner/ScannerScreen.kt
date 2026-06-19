package com.cleanguard.ai.presentation.scanner

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isComplete = uiState.phase == ScanPhase.COMPLETE

    if (!isComplete) {
        ScanningContent(
            navController = navController,
            currentApp = uiState.currentApp,
            progress = uiState.progress,
            onStart = { viewModel.startScan() }
        )
    } else {
        ScanCompleteContent(
            navController = navController,
            onScanAgain = { viewModel.reset() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanningContent(
    navController: NavController,
    currentApp: String,
    progress: Float,
    onStart: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arcRotation"
    )

    LaunchedEffect(Unit) { onStart() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanning...", fontWeight = FontWeight.SemiBold) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Animated scanning ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(210.dp)
            ) {
                Canvas(modifier = Modifier.size(210.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.width / 2f - 5.dp.toPx()
                    val innerRadius = outerRadius - 10.dp.toPx()

                    // Outer ring
                    drawCircle(
                        color = Color(0xFFEEF3FD),
                        radius = outerRadius,
                        style = Stroke(width = 10.dp.toPx())
                    )
                    // Inner ring
                    drawCircle(
                        color = Color(0xFFF0F0F0),
                        radius = innerRadius,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Rotating blue arc
                    val arcInset = 5.dp.toPx()
                    val arcSize = Size(size.width - arcInset * 2, size.height - arcInset * 2)
                    drawArc(
                        color = PrimaryBlue,
                        startAngle = rotation,
                        sweepAngle = 185f,
                        useCenter = false,
                        topLeft = Offset(arcInset, arcInset),
                        size = arcSize,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner filled circle
                    drawCircle(
                        color = Color(0xFFE8F0FE),
                        radius = 55.dp.toPx()
                    )
                }

                Text(text = "🛡", fontSize = 40.sp)
            }

            Text(
                text = "Scanning installed apps...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = "Using AI to check for risks",
                fontSize = 14.sp,
                color = SubtleGray
            )

            // Progress bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { 0.4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryBlue,
                    trackColor = Color(0xFFE8EAED)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Checking 89 of 234 apps", fontSize = 12.sp, color = SubtleGray)
                    Text("40%", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
            }

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = PrimaryBlue,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Currently checking: ${if (currentApp.isNotBlank()) currentApp else "com.lucky.rewards.app"}",
                        fontSize = 12.sp,
                        color = Color(0xFF1A73E8)
                    )
                }
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                border = BorderStroke(1.dp, SubtleGray),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel Scan", color = SubtleGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanCompleteContent(
    navController: NavController,
    onScanAgain: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Complete", fontWeight = FontWeight.SemiBold) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFE8F5E9))
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SafeGreen,
                    modifier = Modifier.size(60.dp)
                )
            }

            Text(
                text = "All done!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = "Scan completed. Here's what we found.",
                fontSize = 14.sp,
                color = SubtleGray
            )

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Scan Summary", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    SummaryRow(label = "High Risk Apps", value = "3", color = DangerRed)
                    SummaryRow(label = "Suspicious Apps", value = "5", color = SuspiciousOrange)
                    SummaryRow(label = "Safe Apps", value = "226", color = SafeGreen)
                    SummaryRow(label = "Total Scanned", value = "234", color = OnSurface)
                }
            }

            Button(
                onClick = { navController.navigate(Screen.AppList.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("View Results", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = onScanAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, PrimaryBlue)
            ) {
                Text("Scan Again", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = PrimaryBlue)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = SubtleGray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
