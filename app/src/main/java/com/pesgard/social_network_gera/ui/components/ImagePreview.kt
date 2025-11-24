package com.pesgard.social_network_gera.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing

/**
 * Componente para mostrar preview de imágenes seleccionadas
 */
@Composable
fun ImagePreview(
    images: List<Uri>,
    onRemoveImage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.small),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = ConnectaSpacing.medium)
    ) {
        itemsIndexed(images) { index, uri ->
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(ConnectaSpacing.small))
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen ${index + 1}",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )

                // Botón para eliminar
                IconButton(
                    onClick = { onRemoveImage(index) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Eliminar imagen",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

