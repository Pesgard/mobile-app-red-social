package com.pesgard.social_network_gera.ui.theme

import androidx.compose.ui.unit.dp

// ============================================================
// SISTEMA DE ESPACIADO
// ============================================================

object ConnectaSpacing {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 24.dp
    val extraExtraLarge = 32.dp
    val huge = 48.dp
    val massive = 64.dp
}

// ============================================================
// DIMENSIONES DE COMPONENTES
// ============================================================

object ConnectaDimensions {
    // App Bar
    val appBarHeight = 64.dp
    val appBarHeightCompact = 56.dp

    // Bottom Navigation
    val bottomNavHeight = 80.dp

    // Avatars
    val avatarSmall = 32.dp
    val avatarMedium = 40.dp
    val avatarLarge = 48.dp
    val avatarExtraLarge = 64.dp
    val avatarHuge = 96.dp
    val avatarStory = 64.dp

    // Buttons
    val buttonHeightSmall = 36.dp
    val buttonHeightMedium = 44.dp
    val buttonHeightLarge = 48.dp
    val buttonMinWidth = 84.dp

    // Icons
    val iconSmall = 16.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconExtraLarge = 28.dp
    val iconHuge = 32.dp

    // Input Fields
    val inputHeight = 48.dp
    val inputHeightLarge = 56.dp
    val textAreaMinHeight = 80.dp

    // Cards
    val cardElevation = 0.dp
    val cardElevationHover = 2.dp

    // Images
    val postImageAspectRatio = 1f // Square
    val storyImageAspectRatio = 9f / 16f // Vertical

    // Dividers
    val dividerThickness = 1.dp

    // Loading indicators
    val loadingIndicatorSize = 40.dp
    val loadingIndicatorSmall = 24.dp

    // Badges
    val badgeSize = 20.dp
    val notificationDot = 10.dp
}

// ============================================================
// VALORES DE OPACIDAD
// ============================================================

object ConnectaOpacity {
    const val disabled = 0.38f
    const val inactive = 0.6f
    const val hover = 0.08f
    const val pressed = 0.12f
    const val dragged = 0.16f
    const val backdrop = 0.6f
    const val backdropDark = 0.8f
}

// ============================================================
// ELEVACIONES (SOMBRAS)
// ============================================================

object ConnectaElevation {
    val none = 0.dp
    val small = 1.dp
    val medium = 2.dp
    val large = 4.dp
    val extraLarge = 8.dp
}

// ============================================================
// BORDER WIDTH
// ============================================================

object ConnectaBorderWidth {
    val thin = 1.dp
    val medium = 2.dp
    val thick = 3.dp
    val extraThick = 4.dp
}