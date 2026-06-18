package com.cleanguard.ai.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD3E2FB),
    secondary = SecondaryTeal,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurface,
    outline = OutlineColor
)

val LocalGrandparentMode = compositionLocalOf { false }

@Composable
fun CleanGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isGrandparentMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        LightColorScheme
    }

    val typography = if (isGrandparentMode) GrandparentTypography else Typography

    CompositionLocalProvider(LocalGrandparentMode provides isGrandparentMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
