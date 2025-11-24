package com.pesgard.social_network_gera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.Primary

/**
 * Componente de input para comentarios
 */
@Composable
fun CommentInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Agregar un comentario...",
    enabled: Boolean = true,
    isSubmitting: Boolean = false,
    replyingTo: Comment? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(ConnectaSpacing.medium),
        horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Campo de texto
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = if (replyingTo != null) {
                        "Respondiendo a ${replyingTo.user?.alias ?: "usuario"}..."
                    } else {
                        placeholder
                    }
                )
            },
            modifier = Modifier.weight(1f),
            enabled = enabled && !isSubmitting,
            singleLine = false,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = CircleShape
        )

        // Botón de envío
        IconButton(
            onClick = onSubmit,
            enabled = enabled && text.trim().isNotEmpty() && !isSubmitting,
            modifier = Modifier
                .size(ConnectaDimensions.iconLarge)
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .size(ConnectaDimensions.iconLarge)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar comentario",
                    tint = if (text.trim().isNotEmpty() && !isSubmitting) {
                        Primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(ConnectaDimensions.iconMedium)
                )
            }
        }
    }

    // Indicador de que está respondiendo a alguien
    if (replyingTo != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ConnectaSpacing.medium),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Respondiendo a ${replyingTo.user?.alias ?: "usuario"}",
                style = com.pesgard.social_network_gera.ui.theme.ConnectaTypography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = ConnectaSpacing.small)
            )
        }
    }
}

