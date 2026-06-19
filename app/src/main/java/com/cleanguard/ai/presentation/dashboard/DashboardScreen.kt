package com.cleanguard.ai.presentation.dashboard

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val currentlyScanningApp by viewModel.currentlyScanningApp.collectAsState()
    val animatedProgress by animateFloatAsState(
        targetValue = scanProgress,
        animationSpec = tween(durationMillis = 400),
        label = "scanProgress"
    )
    val healthScore = uiState.healthScore?.score ?: 0
    val animatedSweep by animateFloatAsState(
        targetValue = healthScore / 100f * 360f,
        animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
        label = "sweep"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "CleanGuard AI",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "AI Phone Health Checkup",
                            fontSize = 12.sp,
                            color = SubtleGray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SubtleGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.Scanner.route) },
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                icon = { Text("🛡") },
                text = { Text("Scan Now", fontWeight = FontWeight.SemiBold) }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceLight) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Scanner.route) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Scanner") },
                    label = { Text("Scanner") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.AppList.route) },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                    label = { Text("Apps") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Report.route) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Report") },
                    label = { Text("Report") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scan progress card (visible only while scanning)
            if (isScanning) {
                ScanProgressCard(
                    progress = animatedProgress,
                    currentApp = currentlyScanningApp
                )
            }

            // Health Score Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Phone Health Score",
                        fontSize = 14.sp,
                        color = SubtleGray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Health ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(180.dp)
                    ) {
                        Canvas(modifier = Modifier.size(180.dp)) {
                            val strokeWidth = 18.dp.toPx()
                            val inset = strokeWidth / 2f
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(inset, inset)

                            drawArc(
                                color = Color(0xFFEEEEEE),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            val arcColor = when {
                                healthScore >= 85 -> SafeGreen
                                healthScore >= 65 -> Color(0xFF8BC34A)
                                healthScore >= 40 -> Color(0xFFFF9800)
                                else -> DangerRed
                            }
                            drawArc(
                                color = arcColor,
                                startAngle = -90f,
                                sweepAngle = animatedSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = PrimaryBlue,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = healthScore.toString(),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                                Text(
                                    text = "out of 100",
                                    fontSize = 12.sp,
                                    color = SubtleGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grade badge
                    val grade = uiState.healthScore?.grade
                    if (grade != null) {
                        val badgeBg = when (healthScore) {
                            in 85..100 -> Color(0xFFE8F5E9)
                            in 65..84 -> Color(0xFFF9FBE7)
                            in 40..64 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                        val badgeColor = when (healthScore) {
                            in 85..100 -> SafeGreen
                            in 65..84 -> Color(0xFF8BC34A)
                            in 40..64 -> ReviewAmber
                            else -> DangerRed
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(badgeBg)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "● ${grade.label}",
                                color = badgeColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.isLoading) "Scanning apps..."
                               else if (uiState.lastScanTimestamp != null) "Scan complete"
                               else "Tap Scan Now to begin",
                        fontSize = 12.sp,
                        color = SubtleGray
                    )
                }
            }

            // Risk Summary
            Text(
                text = "RISK SUMMARY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SubtleGray,
                letterSpacing = 1.sp
            )

            val hs = uiState.healthScore
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiskTile(
                        count = hs?.highRiskApps ?: 0,
                        label = "High Risk\nApps",
                        borderColor = DangerRed,
                        textColor = DangerRed,
                        bgColor = Color(0xFFFFEBEE),
                        modifier = Modifier.weight(1f)
                    )
                    RiskTile(
                        count = hs?.accessibilityRisks ?: 0,
                        label = "Accessibility\nRisks",
                        borderColor = ReviewAmber,
                        textColor = ReviewAmber,
                        bgColor = Color(0xFFFFF8EE),
                        modifier = Modifier.weight(1f)
                    )
                    RiskTile(
                        count = hs?.overlayRisks ?: 0,
                        label = "Overlay\nRisks",
                        borderColor = ReviewAmber,
                        textColor = ReviewAmber,
                        bgColor = Color(0xFFFFF8EE),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiskTile(
                        count = hs?.notificationAbusers ?: 0,
                        label = "Notif.\nSpammers",
                        borderColor = ReviewAmber,
                        textColor = ReviewAmber,
                        bgColor = Color(0xFFFFF8EE),
                        modifier = Modifier.weight(1f)
                    )
                    RiskTile(
                        count = hs?.privacyRisks ?: 0,
                        label = "Privacy\nRisks",
                        borderColor = SuspiciousOrange,
                        textColor = SuspiciousOrange,
                        bgColor = Color(0xFFFBE9E7),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Quick Actions
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SubtleGray,
                letterSpacing = 1.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    QuickActionRow(
                        icon = Icons.Default.Image,
                        title = "Analyze a Screenshot",
                        subtitle = "AI scan of suspicious messages or alerts",
                        onClick = { navController.navigate(Screen.Screenshot.route) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    QuickActionRow(
                        icon = Icons.Default.Language,
                        title = "Chrome Cleanup",
                        subtitle = "Block pop-ups, clear data, check extensions",
                        onClick = { navController.navigate(Screen.Chrome.route) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = DividerColor)
                    QuickActionRow(
                        icon = Icons.Default.Assessment,
                        title = "View Full Report",
                        subtitle = "Detailed AI analysis of all detected threats",
                        onClick = { navController.navigate(Screen.Report.route) }
                    )
                }
            }

            Spacer(modifier = Modifier
                .navigationBarsPadding()
                .height(80.dp))
        }
    }
}

@Composable
private fun ScanProgressCard(
    progress: Float,
    currentApp: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = PrimaryBlue,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Scanning your apps...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryBlue,
                    trackColor = Color(0xFFD2E3FC)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (currentApp.isNotBlank()) currentApp else "Finalizing results...",
                    fontSize = 12.sp,
                    color = PrimaryBlue,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RiskTile(
    count: Int,
    label: String,
    borderColor: Color,
    textColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = SubtleGray,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F0FE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SubtleGray
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SubtleGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
