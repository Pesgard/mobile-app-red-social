package com.pesgard.social_network_gera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// ============================================================
// TEMA PRINCIPAL DE LA APLICACIÓN
// ============================================================

@Composable
fun ConnectaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ConnectaTypography,
        shapes = ConnectaShapes,
        content = content
    )
}