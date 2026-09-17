package com.mobile.sap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Muhfarming theme. Dynamic color is intentionally off so the leaf-green brand
 * stays consistent across devices; instead we ship a hand-tuned light and dark
 * scheme covering every Material3 role.
 */

private val LightColors = lightColorScheme(
    primary = Leaf,
    onPrimary = Cloud,
    primaryContainer = LeafTint,
    onPrimaryContainer = LeafDark,
    secondary = Olive,
    onSecondary = Cloud,
    secondaryContainer = Color_OliveContainer,
    onSecondaryContainer = Color_OliveOnContainer,
    tertiary = Harvest,
    onTertiary = Cloud,
    tertiaryContainer = HarvestTint,
    onTertiaryContainer = Color_HarvestOnContainer,
    background = Sand,
    onBackground = Ink,
    surface = Cloud,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Slate,
    outline = Stone,
    outlineVariant = Mist,
    error = Danger,
    onError = Cloud,
    inverseSurface = Ink,
    inverseOnSurface = Sand,
    scrim = Color_Scrim
)

private val DarkColors = darkColorScheme(
    primary = LeafLight,
    onPrimary = Color(0xFF07230F),
    primaryContainer = LeafTintDark,
    onPrimaryContainer = LeafLight,
    secondary = OliveLight,
    onSecondary = Color(0xFF16220A),
    secondaryContainer = Color(0xFF34401E),
    onSecondaryContainer = OliveLight,
    tertiary = HarvestLight,
    onTertiary = Color(0xFF2E1A05),
    tertiaryContainer = Color(0xFF4A3211),
    onTertiaryContainer = HarvestLight,
    background = NightBg,
    onBackground = Snow,
    surface = NightSurface,
    onSurface = Snow,
    surfaceVariant = NightVariant,
    onSurfaceVariant = Fog,
    outline = NightOutline,
    outlineVariant = NightVariant,
    error = Color(0xFFF2857A),
    onError = Color(0xFF3A0A05),
    inverseSurface = Snow,
    inverseOnSurface = NightBg,
    scrim = Color_Scrim
)

@Composable
fun MuhfarmingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
