package com.pesgard.social_network_gera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================
// MATERIAL 3 COLOR SCHEMES
// ============================================================

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.1f),
    onPrimaryContainer = Primary,

    secondary = PrimaryVariant,
    onSecondary = Color.White,
    secondaryContainer = PrimaryVariant.copy(alpha = 0.1f),
    onSecondaryContainer = PrimaryVariant,

    tertiary = AccentLike,
    onTertiary = Color.White,
    tertiaryContainer = AccentLike.copy(alpha = 0.1f),
    onTertiaryContainer = AccentLike,

    error = AccentDestructive,
    onError = Color.White,
    errorContainer = AccentDestructive.copy(alpha = 0.1f),
    onErrorContainer = AccentDestructive,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Slate100,
    onSurfaceVariant = TextSecondaryLight,

    outline = BorderLight,
    outlineVariant = Slate200,

    scrim = Color.Black.copy(alpha = 0.6f),

    inverseSurface = Slate900,
    inverseOnSurface = Slate50,
    inversePrimary = Primary
)

val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Primary,

    secondary = PrimaryVariant,
    onSecondary = Color.White,
    secondaryContainer = PrimaryVariant.copy(alpha = 0.2f),
    onSecondaryContainer = PrimaryVariant,

    tertiary = AccentLike,
    onTertiary = Color.White,
    tertiaryContainer = AccentLike.copy(alpha = 0.2f),
    onTertiaryContainer = AccentLike,

    error = AccentDestructiveDark,
    onError = Color.White,
    errorContainer = AccentDestructiveDark.copy(alpha = 0.2f),
    onErrorContainer = AccentDestructiveDark,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkAlt,
    onSurfaceVariant = TextSecondaryDark,

    outline = BorderDark,
    outlineVariant = Gray700,

    scrim = Color.Black.copy(alpha = 0.8f),

    inverseSurface = Slate100,
    inverseOnSurface = Slate900,
    inversePrimary = Primary
)

// ============================================================
// EXTENSION PARA COLORES DINÁMICOS SEGÚN TEMA
// ============================================================

val MaterialTheme.connectaColors: ConnectaThemeColors
    @Composable
    get() {
        // Determina si el tema actual es dark basándose en el colorScheme
        val isDark = colorScheme.background == BackgroundDark
        return if (isDark) {
            ConnectaThemeColors.dark
        } else {
            ConnectaThemeColors.light
        }
    }

data class ConnectaThemeColors(
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val surfacePrimary: Color,
    val surfaceSecondary: Color,
    val border: Color,
    val borderSubtle: Color,
    val destructive: Color,
    val iconPrimary: Color,
    val iconSecondary: Color
) {
    companion object {
        val light = ConnectaThemeColors(
            textPrimary = TextPrimaryLight,
            textSecondary = TextSecondaryLight,
            textTertiary = TextTertiaryLight,
            backgroundPrimary = BackgroundLight,
            backgroundSecondary = SurfaceLight,
            surfacePrimary = SurfaceLight,
            surfaceSecondary = Slate100,
            border = BorderLight,
            borderSubtle = Slate200,
            destructive = AccentDestructive,
            iconPrimary = Gray800,
            iconSecondary = Gray500
        )

        val dark = ConnectaThemeColors(
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            textTertiary = TextTertiaryDark,
            backgroundPrimary = BackgroundDark,
            backgroundSecondary = SurfaceDark,
            surfacePrimary = SurfaceDark,
            surfaceSecondary = SurfaceDarkAlt,
            border = BorderDark,
            borderSubtle = Gray700,
            destructive = AccentDestructiveDark,
            iconPrimary = Slate200,
            iconSecondary = Slate400
        )
    }
}