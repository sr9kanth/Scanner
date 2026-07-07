@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.cleanguard.ai.presentation.onboarding

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun finishOnboarding() {
        viewModel.completeOnboarding()
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> PermissionsPage()
                    2 -> ApiKeysPage(viewModel = viewModel)
                    3 -> ReadyPage(onStart = { finishOnboarding() })
                }
            }

            // Bottom navigation bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceLight)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Dot indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (pagerState.currentPage == index) PrimaryBlue else DividerColor,
                                    shape = CircleShape
                                )
                        )
                        if (index < 3) Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                // Skip / Next row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip button (invisible on last page)
                    if (pagerState.currentPage < 3) {
                        TextButton(
                            onClick = { finishOnboarding() }
                        ) {
                            Text(
                                text = "Skip",
                                color = SubtleGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // Next / Get Started button
                    if (pagerState.currentPage < 3) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(
                                text = "Next",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        // On last page the "Start" button is full-width inside ReadyPage itself
                        Spacer(modifier = Modifier.width(80.dp))
                    }
                }
            }
        }
    }
}

// ─── Page 1: Welcome ─────────────────────────────────────────────────────────

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to CleanGuard AI",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your AI-powered phone health guardian. We scan for malware, scam apps, and privacy risks — and explain everything in plain language.",
            fontSize = 14.sp,
            color = SubtleGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// ─── Page 2: Permissions ─────────────────────────────────────────────────────

@Composable
private fun PermissionsPage() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permission states — refreshed on each resume
    var notificationListenerGranted by remember { mutableStateOf(false) }
    var pushNotifGranted by remember { mutableStateOf(false) }
    var mediaGranted by remember { mutableStateOf(false) }
    var usageStatsGranted by remember { mutableStateOf(false) }

    fun refreshPermissions() {
        notificationListenerGranted = androidx.core.app.NotificationManagerCompat
            .getEnabledListenerPackages(context)
            .contains(context.packageName)
        pushNotifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        mediaGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        usageStatsGranted = appOps.unsafeCheckOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    // Refresh on lifecycle resume (user returning from Settings)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            refreshPermissions()
        }
    }

    // Launchers for runtime permissions
    val pushNotifLauncher = rememberLauncherForActivityResult(RequestPermission()) {
        refreshPermissions()
    }
    val mediaLauncher = rememberLauncherForActivityResult(RequestPermission()) {
        refreshPermissions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Required Permissions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "CleanGuard needs these to protect your phone. Tap each one to grant it.",
            fontSize = 14.sp,
            color = SubtleGray,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                PermissionItem(
                    icon = Icons.Default.Notifications,
                    name = "Notification Monitor",
                    description = "Lets CleanGuard detect spam notification apps",
                    granted = notificationListenerGranted,
                    onTap = {
                        context.startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
                HorizontalDivider(color = DividerColor)
                PermissionItem(
                    icon = Icons.Default.NotificationsActive,
                    name = "Push Notifications",
                    description = "Allows CleanGuard to alert you to threats",
                    granted = pushNotifGranted,
                    onTap = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pushNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                HorizontalDivider(color = DividerColor)
                PermissionItem(
                    icon = Icons.Default.Image,
                    name = "Media Access",
                    description = "Lets you share screenshots for AI analysis",
                    granted = mediaGranted,
                    onTap = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            mediaLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
                )
                HorizontalDivider(color = DividerColor)
                PermissionItem(
                    icon = Icons.Default.BarChart,
                    name = "Usage Stats",
                    description = "Shows detailed per-app data",
                    granted = usageStatsGranted,
                    onTap = {
                        context.startActivity(
                            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    name: String,
    description: String,
    granted: Boolean,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = OnSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = SubtleGray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (granted) {
            Box(
                modifier = Modifier
                    .background(
                        color = SafeGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Granted",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .background(
                        color = ReviewAmber.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onTap() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Tap to Enable",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReviewAmber
                )
            }
        }
    }
}

// ─── Page 3: API Keys ─────────────────────────────────────────────────────────

@Composable
private fun ApiKeysPage(viewModel: OnboardingViewModel) {
    var geminiKey by remember { mutableStateOf(viewModel.getGeminiKey()) }
    var deepSeekKey by remember { mutableStateOf(viewModel.getDeepSeekKey()) }
    var virusTotalKey by remember { mutableStateOf(viewModel.getVirusTotalKey()) }
    var keysSaved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(
            text = "AI Analysis Keys",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Optional but recommended. Paste your keys to unlock AI-powered threat analysis.",
            fontSize = 14.sp,
            color = SubtleGray,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = {
                        geminiKey = it
                        keysSaved = false
                    },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("Enter your Gemini API key", color = SubtleGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = SubtleGray)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = deepSeekKey,
                    onValueChange = {
                        deepSeekKey = it
                        keysSaved = false
                    },
                    label = { Text("DeepSeek API Key") },
                    placeholder = { Text("Enter your DeepSeek API key", color = SubtleGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = SubtleGray)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = virusTotalKey,
                    onValueChange = {
                        virusTotalKey = it
                        keysSaved = false
                    },
                    label = { Text("VirusTotal API Key") },
                    placeholder = { Text("Enter your VirusTotal API key", color = SubtleGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = SubtleGray)
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        viewModel.saveKeys(geminiKey, deepSeekKey, virusTotalKey)
                        keysSaved = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Keys", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                if (keysSaved) {
                    Text(
                        text = "Keys saved",
                        fontSize = 12.sp,
                        color = SafeGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Get keys free at: aistudio.google.com · platform.deepseek.com · virustotal.com",
            fontSize = 12.sp,
            color = SubtleGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

// ─── Page 4: Ready ───────────────────────────────────────────────────────────

@Composable
private fun ReadyPage(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SafeGreen,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "You're all set!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = OnSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "CleanGuard will now scan your installed apps and alert you to any risks.",
            fontSize = 14.sp,
            color = SubtleGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Start Protecting My Phone",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}
