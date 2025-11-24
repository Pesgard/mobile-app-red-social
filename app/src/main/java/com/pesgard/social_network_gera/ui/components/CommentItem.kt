package com.pesgard.social_network_gera.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.ui.theme.AccentLike
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.util.toRelativeTimeString

/**
 * Componente para mostrar un comentario individual
 */
@Composable
fun CommentItem(
    comment: Comment,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier,
    showReplies: Boolean = true,
    isReply: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            // Avatar
            AsyncImage(
                model = comment.user?.avatarUrl ?: "",
                contentDescription = "Avatar de ${comment.user?.alias}",
                modifier = Modifier
                    .size(if (isReply) ConnectaDimensions.avatarSmall else ConnectaDimensions.avatarMedium)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
            )


            Spacer(modifier = Modifier.width(ConnectaSpacing.small))

            // Contenido del comentario
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Burbuja del comentario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ConnectaCustomShapes.commentBubble,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(ConnectaSpacing.small)
                    ) {
                        // Nombre del usuario
                        Text(
                            text = comment.user?.alias ?: comment.user?.fullName ?: "Usuario",
                            style = ConnectaTypographyExtensions.username,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Texto del comentario
                        Text(
                            text = comment.text,
                            style = ConnectaTypographyExtensions.postContent,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = ConnectaSpacing.extraSmall)
                        )
                    }
                }

                // Acciones: Like, Reply, Timestamp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = ConnectaSpacing.small,
                            top = ConnectaSpacing.extraSmall
                        ),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timestamp
                    Text(
                        text = comment.createdAt.toRelativeTimeString(),
                        style = ConnectaTypographyExtensions.timestamp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(ConnectaSpacing.medium))

                    // Like
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = "Me gusta",
                            tint = if (comment.isLiked) AccentLike else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ConnectaDimensions.iconSmall)
                        )
                        Text(
                            text = "${comment.likes}",
                            style = ConnectaTypography.bodySmall,
                            color = if (comment.isLiked) AccentLike else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(ConnectaSpacing.medium))

                    // Reply
                    Text(
                        text = "Responder",
                        style = ConnectaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onReplyClick() }
                    )
                }
            }
        }

        // Replies anidados
        if (showReplies && comment.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.padding(top = ConnectaSpacing.small))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = ConnectaDimensions.avatarMedium + ConnectaSpacing.small)
            ) {
                comment.replies.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        onLikeClick = { /* TODO: Implementar like en reply */ },
                        onReplyClick = { /* TODO: Implementar reply a reply */ },
                        isReply = true,
                        showReplies = false // Los replies no tienen sub-replies por ahora
                    )
                    Spacer(modifier = Modifier.padding(top = ConnectaSpacing.small))
                }
            }
        }
    }
}

