package com.cleanguard.ai.presentation.apps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.AppInfo
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*

private fun RiskLevel.color() = when (this) {
    RiskLevel.SAFE -> SafeGreen
    RiskLevel.REVIEW -> ReviewAmber
    RiskLevel.SUSPICIOUS -> SuspiciousOrange
    RiskLevel.REMOVE_IMMEDIATELY -> DangerRed
}

private fun RiskLevel.bgColor() = when (this) {
    RiskLevel.SAFE -> Color(0xFFE8F5E9)
    RiskLevel.REVIEW -> Color(0xFFFFF8E1)
    RiskLevel.SUSPICIOUS -> Color(0xFFFBE9E7)
    RiskLevel.REMOVE_IMMEDIATELY -> Color(0xFFFFEBEE)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    navController: NavController,
    viewModel: AppListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters = listOf("All", "High Risk", "Suspicious", "Accessibility", "Overlay")
    val filterEnums = listOf(
        AppFilter.ALL, AppFilter.HIGH_RISK, AppFilter.SUSPICIOUS,
        AppFilter.ACCESSIBILITY, AppFilter.OVERLAY
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Analysis", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isLoading) {
                        Text(
                            text = "${uiState.totalCount} apps",
                            fontSize = 13.sp,
                            color = SubtleGray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = { viewModel.rescan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rescan", tint = PrimaryBlue)
                        }
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search field
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search apps...", color = SubtleGray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SubtleGray)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { i, label ->
                    FilterChip(
                        selected = uiState.filter == filterEnums[i],
                        onClick = { viewModel.setFilter(filterEnums[i]) },
                        label = { Text(label, fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Scanning installed apps...", color = SubtleGray, fontSize = 14.sp)
                        }
                    }
                }
                uiState.apps.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No apps match this filter", color = OnSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.apps, key = { it.packageName }) { app ->
                            AppItemCard(
                                app = app,
                                onClick = {
                                    navController.navigate(Screen.AppDetail.createRoute(app.packageName))
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItemCard(app: AppInfo, onClick: () -> Unit) {
    val riskColor = app.riskLevel.color()
    val riskBg = app.riskLevel.bgColor()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(riskColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(riskBg)
                ) {
                    Text(
                        text = app.appName.first().uppercaseChar().toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = app.packageName,
                        fontSize = 11.sp,
                        color = SubtleGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(riskBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = app.riskLevel.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = riskColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = app.riskScore.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor
                    )
                    Text(text = "score", fontSize = 10.sp, color = SubtleGray)
                }
            }
        }
    }
}
