package com.cleanguard.ai.presentation.apps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.presentation.components.EmptyState
import com.cleanguard.ai.presentation.components.RiskBadge
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.SubtleGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    navController: NavController,
    viewModel: AppListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Installed Apps") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppFilter.values()) { filter ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.apps.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.AppsOutage,
                    title = "No apps found",
                    subtitle = "Run a scan first to see your installed apps",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.apps, key = { it.packageName }) { app ->
                        AppListItem(app = app, onClick = {
                            navController.navigate(Screen.AppDetail.createRoute(app.packageName))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = app.appName.first().uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(text = app.packageName, style = MaterialTheme.typography.labelSmall, color = SubtleGray, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                RiskBadge(riskLevel = app.riskLevel)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = app.riskScore.toString(), fontWeight = FontWeight.Bold, color = when(app.riskLevel) {
                    RiskLevel.SAFE -> com.cleanguard.ai.presentation.theme.SafeGreen
                    RiskLevel.REVIEW -> com.cleanguard.ai.presentation.theme.ReviewAmber
                    RiskLevel.SUSPICIOUS -> com.cleanguard.ai.presentation.theme.SuspiciousOrange
                    RiskLevel.REMOVE_IMMEDIATELY -> com.cleanguard.ai.presentation.theme.DangerRed
                })
                Text(text = "score", style = MaterialTheme.typography.labelSmall, color = SubtleGray)
            }
        }
    }
}
