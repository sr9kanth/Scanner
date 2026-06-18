package com.cleanguard.ai.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cleanguard.ai.domain.model.HealthGrade
import com.cleanguard.ai.domain.model.RiskLevel
import com.cleanguard.ai.presentation.theme.*

@Composable
fun RiskBadge(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (color, icon) = when (riskLevel) {
        RiskLevel.SAFE -> SafeGreen to Icons.Default.CheckCircle
        RiskLevel.REVIEW -> ReviewAmber to Icons.Default.Info
        RiskLevel.SUSPICIOUS -> SuspiciousOrange to Icons.Default.Warning
        RiskLevel.REMOVE_IMMEDIATELY -> DangerRed to Icons.Default.Dangerous
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = riskLevel.label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun HealthScoreRing(
    score: Int,
    grade: HealthGrade,
    modifier: Modifier = Modifier,
    size: Int = 140
) {
    val color = when (grade) {
        HealthGrade.EXCELLENT -> ScoreExcellent
        HealthGrade.GOOD -> ScoreGood
        HealthGrade.NEEDS_ATTENTION -> ScoreAttention
        HealthGrade.AT_RISK -> ScoreAtRisk
    }
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 10.dp,
            trackColor = color.copy(alpha = 0.15f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = score.toString(), fontSize = (size / 4).sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = grade.label, fontSize = (size / 10).sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun StatCard(
    count: Int,
    label: String,
    color: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = SubtleGray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = SubtleGray)
        Spacer(Modifier.height(16.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = OnSurface)
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = SubtleGray, textAlign = TextAlign.Center)
    }
}

@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) DangerRed else MaterialTheme.colorScheme.primary
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold)
    }
}
