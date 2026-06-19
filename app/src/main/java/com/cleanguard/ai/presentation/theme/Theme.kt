package com.cleanguard.ai.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD3E2FB),
    secondary = SecondaryTeal,
    background = BackgroundLight,
    onBackground = OnSurface,
    surface = SurfaceLight,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurface,
    outline = OutlineColor
)

val LocalGrandparentMode = compositionLocalOf { false }

@Composable
fun CleanGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isGrandparentMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val typography = if (isGrandparentMode) GrandparentTypography else Typography

    CompositionLocalProvider(LocalGrandparentMode provides isGrandparentMode) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = typography,
            content = content
        )
    }
}
