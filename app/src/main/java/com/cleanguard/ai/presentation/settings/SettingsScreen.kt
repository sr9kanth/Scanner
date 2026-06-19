package com.cleanguard.ai.presentation.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }
    var notificationAccessEnabled by remember { mutableStateOf(false) }
    var geminiKey by remember(uiState.geminiKey) { mutableStateOf(uiState.geminiKey) }
    var deepSeekKey by remember(uiState.deepSeekKey) { mutableStateOf(uiState.deepSeekKey) }
    var virusTotalKey by remember(uiState.virusTotalKey) { mutableStateOf(uiState.virusTotalKey) }
    var selectedFrequency by remember { mutableStateOf("Daily") }
    var frequencyExpanded by remember { mutableStateOf(false) }
    val frequencies = listOf("Every 6 hours", "Daily", "Weekly", "Manual only")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Accessibility & Permissions
            SettingsSection(title = "Accessibility & Permissions") {
                SettingsSwitchRow(
                    icon = Icons.Default.Accessibility,
                    title = "Accessibility Service",
                    subtitle = "Monitor app behavior in real-time",
                    checked = accessibilityEnabled,
                    onCheckedChange = { accessibilityEnabled = it }
                )
                HorizontalDivider(color = DividerColor)
                SettingsSwitchRow(
                    icon = Icons.Default.Layers,
                    title = "Overlay Permission",
                    subtitle = "Show risk alerts over other apps",
                    checked = overlayEnabled,
                    onCheckedChange = { overlayEnabled = it }
                )
                HorizontalDivider(color = DividerColor)
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "Notification Access",
                    subtitle = "Monitor notification activity",
                    checked = notificationAccessEnabled,
                    onCheckedChange = { notificationAccessEnabled = it }
                )
            }

            // AI Configuration
            SettingsSection(title = "AI Configuration") {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = geminiKey,
                        onValueChange = { geminiKey = it },
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
                        onValueChange = { deepSeekKey = it },
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
                        onValueChange = { virusTotalKey = it },
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
                        onClick = { viewModel.saveApiKeys(geminiKey, deepSeekKey, virusTotalKey) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save API Keys", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    if (uiState.keysSaved) {
                        Text("Keys saved", fontSize = 12.sp, color = SafeGreen, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Appearance
            SettingsSection(title = "Appearance") {
                SettingsSwitchRow(
                    icon = Icons.Default.Elderly,
                    title = "Grandparent Mode",
                    subtitle = "Larger text, simpler actions",
                    checked = uiState.isGrandparentMode,
                    onCheckedChange = { viewModel.setGrandparentMode(it) }
                )
            }

            // Scan Schedule
            SettingsSection(title = "Scan Schedule") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Scan Frequency", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
                            Text("How often to auto-scan", fontSize = 12.sp, color = SubtleGray)
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = frequencyExpanded,
                        onExpandedChange = { frequencyExpanded = it }
                    ) {
                        OutlinedButton(
                            onClick = { frequencyExpanded = true },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(selectedFrequency, fontSize = 12.sp, color = OnSurface)
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = SubtleGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = frequencyExpanded,
                            onDismissRequest = { frequencyExpanded = false }
                        ) {
                            frequencies.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq, fontSize = 13.sp) },
                                    onClick = {
                                        selectedFrequency = freq
                                        frequencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // About
            SettingsSection(title = "About") {
                SettingsNavRow(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "CleanGuard AI v1.0.0"
                )
                HorizontalDivider(color = DividerColor)
                SettingsNavRow(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy"
                )
                HorizontalDivider(color = DividerColor)
                SettingsNavRow(
                    icon = Icons.Default.Description,
                    title = "Terms of Service",
                    subtitle = "Read the terms of service"
                )
            }

            // Danger Zone
            Text(
                text = "DANGER ZONE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DangerRed,
                letterSpacing = 1.sp
            )

            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, DangerRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clear All Data",
                    color = DangerRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier
                .navigationBarsPadding()
                .height(16.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SubtleGray,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
            Text(subtitle, fontSize = 12.sp, color = SubtleGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryBlue)
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
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
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = OnSurface)
            Text(subtitle, fontSize = 12.sp, color = SubtleGray)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SubtleGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
