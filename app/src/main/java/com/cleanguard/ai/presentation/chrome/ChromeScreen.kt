package com.cleanguard.ai.presentation.chrome

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChromeScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chrome Cleanup", fontWeight = FontWeight.SemiBold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Green status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Chrome is installed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Text(
                text = "Follow these steps to secure your Chrome browser and reduce unwanted notifications and pop-ups.",
                fontSize = 13.sp,
                color = SubtleGray,
                lineHeight = 20.sp
            )

            // Step 1 - completed
            ChromeStepCard(
                stepNumber = 1,
                title = "Disable Notifications",
                isCompleted = true,
                isActive = false,
                pill = "Settings → Apps → Chrome → Notifications → Off"
            )

            // Step 2 - completed
            ChromeStepCard(
                stepNumber = 2,
                title = "Clear Browsing Data",
                isCompleted = true,
                isActive = false,
                pill = "Chrome Menu → History → Clear Browsing Data"
            )

            // Step 3 - active
            ChromeStepCard(
                stepNumber = 3,
                title = "Block Pop-ups",
                isCompleted = false,
                isActive = true,
                pill = "Chrome Menu → Settings → Site Settings → Pop-ups"
            )

            // Step 4 - pending
            ChromeStepCard(
                stepNumber = 4,
                title = "Check Extensions",
                isCompleted = false,
                isActive = false,
                pill = "Chrome Menu → More tools → Extensions"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {},
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChromeStepCard(
    stepNumber: Int,
    title: String,
    isCompleted: Boolean,
    isActive: Boolean,
    pill: String
) {
    val cardAlpha = if (!isCompleted && !isActive) 0.7f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight.copy(alpha = cardAlpha)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 3.dp else 1.dp
        ),
        border = if (isActive) BorderStroke(1.5.dp, PrimaryBlue) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Step indicator
            if (isCompleted) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SafeGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) PrimaryBlue else Color(0xFFE0E0E0))
                ) {
                    Text(
                        text = stepNumber.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else SubtleGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isCompleted) "$title ✓" else title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) SafeGreen else OnSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF5F5F5))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = pill,
                        fontSize = 11.sp,
                        color = SubtleGray
                    )
                }
            }
        }
    }
}
