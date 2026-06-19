package com.cleanguard.ai.presentation.chrome

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.cleanguard.ai.presentation.theme.*

private const val CHROME_PACKAGE = "com.android.chrome"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val chromeInstalled = remember {
        try {
            context.packageManager.getPackageInfo(CHROME_PACKAGE, 0)
            true
        } catch (e: Exception) { false }
    }

    fun openChrome() {
        val launch = context.packageManager.getLaunchIntentForPackage(CHROME_PACKAGE)
        if (launch != null) {
            context.startActivity(launch)
        } else {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://")))
        }
    }

    fun openChromeNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, CHROME_PACKAGE)
        }
        context.startActivity(intent)
    }

    fun openChromeAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$CHROME_PACKAGE")
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chrome Cleanup", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceLight,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
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
            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (chromeInstalled) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (chromeInstalled) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (chromeInstalled) SafeGreen else DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (chromeInstalled) "Chrome is installed" else "Chrome not found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (chromeInstalled) Color(0xFF2E7D32) else DangerRed
                    )
                }
            }

            Text(
                text = "Use the steps below to block Chrome pop-ups and reduce unwanted notifications. Each button opens the relevant settings screen.",
                fontSize = 13.sp,
                color = SubtleGray,
                lineHeight = 20.sp
            )

            // Step 1 — Disable Notifications
            ChromeStepCard(
                stepNumber = 1,
                title = "Disable Chrome Notifications",
                description = "Stop Chrome from sending pop-up notifications from websites.",
                buttonLabel = "Open Notification Settings",
                onAction = { openChromeNotificationSettings() }
            )

            // Step 2 — Clear Browsing Data
            ChromeStepCard(
                stepNumber = 2,
                title = "Clear Browsing Data",
                description = "Remove cookies and site data that may cause unwanted pop-ups. Opens Chrome's app info so you can clear storage.",
                buttonLabel = "Open Chrome App Info",
                onAction = { openChromeAppSettings() }
            )

            // Step 3 — Block Pop-ups in Chrome
            ChromeStepCard(
                stepNumber = 3,
                title = "Block Pop-ups in Chrome",
                description = "Inside Chrome: tap Menu (⋮) → Settings → Site Settings → Pop-ups and redirects → turn OFF.",
                buttonLabel = "Open Chrome",
                onAction = { openChrome() }
            )

            // Step 4 — Check for suspicious sites
            ChromeStepCard(
                stepNumber = 4,
                title = "Review Site Permissions",
                description = "Inside Chrome: tap Menu (⋮) → Settings → Site Settings → Notifications — remove any sites you don't recognise.",
                buttonLabel = "Open Chrome",
                onAction = { openChrome() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { openChrome() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Chrome",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier
                .navigationBarsPadding()
                .height(16.dp))
        }
    }
}

@Composable
private fun ChromeStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
            ) {
                Text(
                    text = stepNumber.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = SubtleGray,
                    lineHeight = 18.sp
                )
                OutlinedButton(
                    onClick = onAction,
                    border = BorderStroke(1.dp, PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(buttonLabel, color = PrimaryBlue, fontSize = 12.sp)
                }
            }
        }
    }
}
