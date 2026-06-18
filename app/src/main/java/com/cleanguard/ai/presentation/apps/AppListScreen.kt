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
import com.cleanguard.ai.presentation.navigation.Screen
import com.cleanguard.ai.presentation.theme.*

data class AppDisplayItem(
    val appName: String,
    val packageName: String,
    val riskLabel: String,
    val riskColor: Color,
    val riskBgColor: Color,
    val score: Int,
    val borderColor: Color
)

private val staticApps = listOf(
    AppDisplayItem(
        appName = "SystemCleaner Pro",
        packageName = "com.system.cleaner.pro",
        riskLabel = "High Risk",
        riskColor = DangerRed,
        riskBgColor = Color(0xFFFFEBEE),
        score = 94,
        borderColor = DangerRed
    ),
    AppDisplayItem(
        appName = "Lucky Rewards",
        packageName = "com.lucky.rewards.app",
        riskLabel = "Suspicious",
        riskColor = SuspiciousOrange,
        riskBgColor = Color(0xFFFBE9E7),
        score = 67,
        borderColor = SuspiciousOrange
    ),
    AppDisplayItem(
        appName = "Google Maps",
        packageName = "com.google.android.apps.maps",
        riskLabel = "Safe",
        riskColor = SafeGreen,
        riskBgColor = Color(0xFFE8F5E9),
        score = 8,
        borderColor = SafeGreen
    ),
    AppDisplayItem(
        appName = "Calculator",
        packageName = "com.android.calculator2",
        riskLabel = "Safe",
        riskColor = SafeGreen,
        riskBgColor = Color(0xFFE8F5E9),
        score = 4,
        borderColor = SafeGreen
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    navController: NavController,
    viewModel: AppListViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "High Risk", "Suspicious", "Accessibility", "Overlay")

    val filteredApps = staticApps.filter { app ->
        val matchesSearch = searchQuery.isEmpty() ||
            app.appName.contains(searchQuery, ignoreCase = true) ||
            app.packageName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == "All" ||
            (selectedFilter == "High Risk" && app.riskLabel == "High Risk") ||
            (selectedFilter == "Suspicious" && app.riskLabel == "Suspicious")
        matchesSearch && matchesFilter
    }

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
                    Text(
                        text = "234 apps",
                        fontSize = 13.sp,
                        color = SubtleGray,
                        modifier = Modifier.padding(end = 16.dp)
                    )
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
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
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
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 13.sp) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredApps) { app ->
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

@Composable
private fun AppItemCard(
    app: AppDisplayItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(app.borderColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(app.riskBgColor)
                ) {
                    Text(
                        text = app.appName.first().uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = app.riskColor
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
                            .background(app.riskBgColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = app.riskLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = app.riskColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = app.score.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = app.riskColor
                    )
                    Text(text = "score", fontSize = 10.sp, color = SubtleGray)
                }
            }
        }
    }
}
