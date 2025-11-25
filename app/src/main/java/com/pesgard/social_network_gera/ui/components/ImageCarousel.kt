package com.pesgard.social_network_gera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography

/**
 * Carousel de imágenes para mostrar múltiples imágenes en un post
 * Versión simplificada sin HorizontalPager (se puede mejorar después)
 */
@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    Box(modifier = modifier) {
        // Mostrar imagen actual
        AsyncImage(
            model = images[currentIndex],
            contentDescription = "Imagen ${currentIndex + 1} de ${images.size}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onError = { error ->
                android.util.Log.e("ImageCarousel", "Error loading image ${currentIndex + 1}: ${images[currentIndex]}", error.result.throwable)
            },
            onSuccess = {
                android.util.Log.d("ImageCarousel", "Image ${currentIndex + 1} loaded successfully")
            }
        )

        // Indicadores de página (solo si hay más de una imagen)
        if (images.size > 1) {
            // Contador de imágenes
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(ConnectaSpacing.small)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .padding(horizontal = ConnectaSpacing.small, vertical = ConnectaSpacing.extraSmall)
            ) {
                Text(
                    text = "${currentIndex + 1}/${images.size}",
                    style = ConnectaTypography.bodySmall,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // Indicadores de página
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ConnectaSpacing.small),
                horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.extraSmall)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                }
                            )
                            .clickable { currentIndex = index }
                    )
                }
            }
        }
    }
}

