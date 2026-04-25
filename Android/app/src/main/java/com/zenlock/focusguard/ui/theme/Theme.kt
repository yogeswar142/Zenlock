package com.zenlock.focusguard.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Zenlock Material 3 dark color scheme – "Digital Sanctuary."
 */
private val ZenlockColorScheme = darkColorScheme(
    primary = ZenPrimaryContainer,
    onPrimary = Color.White,
    primaryContainer = ZenPrimaryContainer,
    onPrimaryContainer = ZenOnPrimaryContainer,
    inversePrimary = ZenInversePrimary,
    secondary = ZenSecondary,
    onSecondary = Color.Black,
    secondaryContainer = ZenSecondaryContainer,
    tertiary = ZenTertiary,
    tertiaryContainer = ZenTertiaryContainer,
    background = ZenBackground,
    onBackground = ZenOnBackground,
    surface = ZenSurface,
    onSurface = ZenOnSurface,
    surfaceVariant = ZenSurfaceVariant,
    onSurfaceVariant = ZenOnSurfaceVariant,
    surfaceContainerLowest = ZenSurfaceContainerLowest,
    surfaceContainerLow = ZenSurfaceContainerLow,
    surfaceContainer = ZenSurfaceContainer,
    surfaceContainerHigh = ZenSurfaceContainerHigh,
    surfaceContainerHighest = ZenSurfaceContainerHighest,
    surfaceBright = ZenSurfaceBright,
    surfaceDim = ZenSurfaceDim,
    outline = ZenOutline,
    outlineVariant = ZenOutlineVariant,
    error = ZenError,
    onError = ZenOnError,
    errorContainer = ZenErrorContainer,
)

@Composable
fun FocusGuardTheme(content: @Composable () -> Unit) {
    val colorScheme = ZenlockColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = Color.Transparent.toArgb()
                it.navigationBarColor = ZenBackground.toArgb()
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZenlockTypography,
        content = content
    )
}
