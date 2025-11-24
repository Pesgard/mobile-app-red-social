package com.pesgard.social_network_gera.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================
// MATERIAL 3 SHAPES
// ============================================================

val ConnectaShapes = Shapes(
    // Extra Small - Para elementos muy pequeños (chips, badges)
    extraSmall = RoundedCornerShape(4.dp),

    // Small - Para botones pequeños, tarjetas compactas
    small = RoundedCornerShape(8.dp),

    // Medium - Para la mayoría de componentes (cards, buttons)
    medium = RoundedCornerShape(12.dp),

    // Large - Para modales, bottom sheets
    large = RoundedCornerShape(16.dp),

    // Extra Large - Para diálogos grandes
    extraLarge = RoundedCornerShape(28.dp)
)

// ============================================================
// CUSTOM SHAPES ADICIONALES
// ============================================================

object ConnectaCustomShapes {
    // Base radius
    val roundedDefault = RoundedCornerShape(4.dp)
    val roundedLg = RoundedCornerShape(8.dp)
    val roundedXl = RoundedCornerShape(12.dp)
    val rounded2Xl = RoundedCornerShape(16.dp)
    val rounded3Xl = RoundedCornerShape(20.dp)
    val roundedFull = RoundedCornerShape(9999.dp)

    // Shapes específicas para componentes
    val button = RoundedCornerShape(12.dp)
    val card = RoundedCornerShape(12.dp)
    val input = RoundedCornerShape(12.dp)
    val dialog = RoundedCornerShape(16.dp)
    val bottomSheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val avatar = RoundedCornerShape(9999.dp)
    val image = RoundedCornerShape(12.dp)
    val chip = RoundedCornerShape(9999.dp)
    val badge = RoundedCornerShape(9999.dp)

    // Shapes para posts y contenido
    val postImage = RoundedCornerShape(12.dp)
    val storyRing = RoundedCornerShape(9999.dp)
    val commentBubble = RoundedCornerShape(12.dp)

    // Shapes para navegación
    val navigationBar = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    val fab = RoundedCornerShape(16.dp)
}