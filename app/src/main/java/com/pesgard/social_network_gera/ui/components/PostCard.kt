package com.pesgard.social_network_gera.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import com.pesgard.social_network_gera.util.ensureImagePrefix
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.ui.theme.AccentLike
import com.pesgard.social_network_gera.ui.theme.AccentStar
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.util.toRelativeTimeString

/**
 * Componente de tarjeta de publicación
 */
@Composable
fun PostCard(
    post: Post,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick() },
        shape = ConnectaCustomShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ConnectaSpacing.medium)
        ) {
            // Header: Avatar + Nombre + Fecha + More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    AsyncImage(
                        model = post.user?.avatarUrl ?: "",
                        contentDescription = "Avatar de ${post.user?.alias}",
                        modifier = Modifier
                            .size(ConnectaDimensions.avatarMedium)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                        error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                    )

                    Spacer(modifier = Modifier.width(ConnectaSpacing.small))

                    // Nombre y fecha
                    Column {
                        Text(
                            text = post.user?.alias ?: post.user?.fullName ?: "Usuario",
                            style = ConnectaTypographyExtensions.username,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = post.createdAt.toRelativeTimeString(),
                            style = ConnectaTypographyExtensions.timestamp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Botón de más opciones
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(ConnectaDimensions.iconMedium)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

            // Título
            if (post.title.isNotEmpty()) {
                Text(
                    text = post.title,
                    style = ConnectaTypographyExtensions.appBarTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = ConnectaSpacing.small)
                )
            }

            // Descripción
            if (post.description.isNotEmpty()) {
                Text(
                    text = post.description,
                    style = ConnectaTypographyExtensions.postContent,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = ConnectaSpacing.medium)
                )
            }

            // Imágenes
            if (post.images.isNotEmpty()) {
                android.util.Log.d("PostCard", "Post has ${post.images.size} images")
                post.images.forEachIndexed { index, image ->
                    android.util.Log.d("PostCard", "Image $index: ${image.take(100)}...") // Mostrar primeros 100 chars
                }
                
                if (post.images.size == 1) {
                    // Imagen única
                    AsyncImage(
                        model = post.images[0].ensureImagePrefix(),
                        contentDescription = "Imagen del post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(ConnectaCustomShapes.postImage),
                        contentScale = ContentScale.Crop,
                        onError = { error ->
                            android.util.Log.e("PostCard", "Error loading image: ${post.images[0]}", error.result.throwable)
                        },
                        onSuccess = {
                            android.util.Log.d("PostCard", "Image loaded successfully")
                        }
                    )
                } else {
                    // Múltiples imágenes - usar carousel
                    ImageCarousel(
                        images = post.images.map { it.ensureImagePrefix() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))
            }

            // Acciones: Like, Dislike, Comentarios, Favorito
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like y Dislike
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = "Me gusta",
                            tint = if (post.isLiked) AccentLike else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ConnectaDimensions.iconMedium)
                        )
                        Text(
                            text = "${post.likes}",
                            style = ConnectaTypography.bodyMedium,
                            color = if (post.isLiked) AccentLike else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dislike
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDislikeClick() }
                    ) {
                        Icon(
                            imageVector = if (post.isDisliked) {
                                Icons.Filled.ThumbDown
                            } else {
                                Icons.Outlined.ThumbDown
                            },
                            contentDescription = "No me gusta",
                            tint = if (post.isDisliked) AccentLike else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ConnectaDimensions.iconMedium)
                        )
                        Text(
                            text = "${post.dislikes}",
                            style = ConnectaTypography.bodyMedium,
                            color = if (post.isDisliked) AccentLike else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Comentarios
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comentarios",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(ConnectaDimensions.iconMedium)
                    )
                    Text(
                        text = "${post.commentsCount}",
                        style = ConnectaTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Favorito
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(ConnectaDimensions.iconMedium)
                ) {
                    Icon(
                        imageVector = if (post.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = if (post.isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (post.isFavorite) AccentStar else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

