package com.cleanguard.ai.presentation.settings

import android.content.Intent
import android.provider.Settings
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
import com.cleanguard.ai.presentation.theme.SubtleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        ) {
            SettingSection(title = "Display Mode") {
                SettingToggle(
                    icon = Icons.Default.FamilyRestroom,
                    title = "Parent Mode",
                    subtitle = "Simpler language for parents",
                    checked = uiState.isParentMode,
                    onToggle = viewModel::setParentMode
                )
                SettingToggle(
                    icon = Icons.Default.Elderly,
                    title = "Grandparent Mode",
                    subtitle = "Extra large text and one step at a time",
                    checked = uiState.isGrandparentMode,
                    onToggle = viewModel::setGrandparentMode
                )
            }

            SettingSection(title = "Permissions") {
                SettingAction(
                    icon = Icons.Default.Notifications,
                    title = "Notification Access",
                    subtitle = "Required to monitor notification frequency",
                    onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                )
                SettingAction(
                    icon = Icons.Default.Security,
                    title = "Usage Access",
                    subtitle = "Allows deeper app behavior analysis",
                    onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
                )
            }

            SettingSection(title = "About") {
                SettingInfo(icon = Icons.Default.Info, title = "Version", value = "1.0.0")
                SettingInfo(icon = Icons.Default.Shield, title = "AI Models", value = "Gemini 2.5 Flash + DeepSeek")
                SettingInfo(icon = Icons.Default.Lock, title = "Privacy", value = "No personal data uploaded")
            }
        }
    }
}

@Composable
fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun SettingToggle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SubtleGray) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onToggle) }
    )
}

@Composable
fun SettingAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = SubtleGray) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SubtleGray) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SettingInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Text(value, style = MaterialTheme.typography.bodySmall, color = SubtleGray) }
    )
}
