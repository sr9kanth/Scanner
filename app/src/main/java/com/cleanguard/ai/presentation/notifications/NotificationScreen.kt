package com.cleanguard.ai.presentation.notifications

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.cleanguard.ai.presentation.theme.*

data class NotificationAppItem(
    val appName: String,
    val packageName: String,
    val badgeLabel: String,
    val badgeColor: Color,
    val badgeBg: Color,
    val borderColor: Color,
    val weeklyCount: String,
    val dailyRate: String,
    val totalCount: String,
    val countColor: Color,
    val showDisableButton: Boolean
)

private val notificationApps = listOf(
    NotificationAppItem(
        appName = "Lucky Rewards",
        packageName = "com.lucky.rewards.app",
        badgeLabel = "Spammy",
        badgeColor = DangerRed,
        badgeBg = Color(0xFFFFEBEE),
        borderColor = Color(0xFFFF5722),
        weeklyCount = "248 this week",
        dailyRate = "35.4/day",
        totalCount = "891 total",
        countColor = DangerRed,
        showDisableButton = true
    ),
    NotificationAppItem(
        appName = "GigaPromo Deals",
        packageName = "com.gigapromo.deals",
        badgeLabel = "Spammy",
        badgeColor = ReviewAmber,
        badgeBg = Color(0xFFFFF3E0),
        borderColor = Color(0xFFFF9800),
        weeklyCount = "156 this week",
        dailyRate = "22.3/day",
        totalCount = "612 total",
        countColor = ReviewAmber,
        showDisableButton = true
    ),
    NotificationAppItem(
        appName = "Weather App",
        packageName = "com.weather.forecast",
        badgeLabel = "Normal",
        badgeColor = SafeGreen,
        badgeBg = Color(0xFFE8F5E9),
        borderColor = Color(0xFFE0E0E0),
        weeklyCount = "14 this week",
        dailyRate = "2.0/day",
        totalCount = "56 total",
        countColor = SubtleGray,
        showDisableButton = false
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Monitor", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8EE)),
                border = BorderStroke(1.dp, SuspiciousOrange.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE0B2))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = SuspiciousOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "5 apps sending too many notifications",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Text(
                            text = "These apps are flagged as notification spammers and may be draining your attention and battery.",
                            fontSize = 12.sp,
                            color = SubtleGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            notificationApps.forEach { app ->
                NotificationAppCard(app = app)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationAppCard(app: NotificationAppItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left colored border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(app.borderColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = app.appName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = app.packageName,
                            fontSize = 11.sp,
                            color = SubtleGray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(app.badgeBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = app.badgeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = app.badgeColor
                        )
                    }
                }

                // Stats chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatChip(label = app.weeklyCount, color = if (app.showDisableButton) ReviewAmber else SubtleGray)
                    StatChip(label = app.dailyRate, color = if (app.showDisableButton) ReviewAmber else SubtleGray)
                    StatChip(label = app.totalCount, color = app.countColor)
                }

                if (app.showDisableButton) {
                    OutlinedButton(
                        onClick = {},
                        border = BorderStroke(1.5.dp, SuspiciousOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = SuspiciousOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Disable Notifications",
                            color = SuspiciousOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
