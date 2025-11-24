package com.pesgard.social_network_gera.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pesgard.social_network_gera.R

// ============================================================
// FONT FAMILY: PLUS JAKARTA SANS
// ============================================================

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_extralight, FontWeight.ExtraLight),        // 200
    Font(R.font.plus_jakarta_sans_light, FontWeight.Light),                  // 300
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),               // 400
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),                // 500
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),            // 600
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),                    // 700
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),          // 800

    // Italic variants
    Font(R.font.plus_jakarta_sans_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.plus_jakarta_sans_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic)
)

// ============================================================
// MATERIAL 3 TYPOGRAPHY
// ============================================================

val ConnectaTypography = Typography(
    // Display - Para títulos grandes
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headlines - Para encabezados
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Titles - Para títulos de secciones
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.015).sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    // Body - Para contenido general
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Labels - Para botones y etiquetas
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ============================================================
// EXTENSIONES DE TIPOGRAFÍA PERSONALIZADAS
// ============================================================

object ConnectaTypographyExtensions {
    // App Bar Title
    val appBarTitle = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.015).sp
    )

    // Username
    val username = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.015).sp
    )

    // Post Content
    val postContent = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Timestamp
    val timestamp = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // Caption
    val caption = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // Button Text
    val buttonText = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.015.sp
    )

    // Stats Number
    val statsNumber = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    )

    // Stats Label
    val statsLabel = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}