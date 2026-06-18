package com.cleanguard.ai.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.cleanguard.ai.domain.model.HealthGrade
import com.cleanguard.ai.presentation.theme.*

@Composable
fun HealthRingGauge(
    score: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val animatedSweep by animateFloatAsState(
        targetValue = score / 100f * 360f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "sweep"
    )

    val ringColor = when {
        score >= 75 -> SafeGreen
        score >= 50 -> ReviewAmber
        else -> DangerRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = (size.toPx() * 0.1f)
            val inset = strokeWidth / 2f
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            // Gray track
            drawArc(
                color = Color(0xFFEEEEEE),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Colored arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = "out of 100",
                fontSize = (size.value * 0.09f).sp,
                color = SubtleGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RiskBadge(
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithBack(
    title: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceLight,
            titleContentColor = OnSurface
        ),
        modifier = modifier
    )
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color = PrimaryBlue,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OnSurface)
            }
            content()
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isDestructive) Color(0xFFF44336) else PrimaryBlue
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.White)
    }
}

@Composable
fun HealthScoreRing(
    score: Int,
    grade: HealthGrade,
    size: Int,
    modifier: Modifier = Modifier
) {
    HealthRingGauge(
        score = score,
        size = size.dp,
        modifier = modifier
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SubtleGray,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = OnSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = SubtleGray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun RiskSummaryTile(
    count: Int,
    label: String,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left colored border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.Start
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
