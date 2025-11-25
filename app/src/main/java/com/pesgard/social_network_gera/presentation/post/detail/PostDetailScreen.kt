package com.pesgard.social_network_gera.presentation.post.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.ui.components.CommentInput
import com.pesgard.social_network_gera.ui.components.CommentItem
import com.pesgard.social_network_gera.ui.components.EmptyState
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.ImageCarousel
import com.pesgard.social_network_gera.ui.theme.AccentLike
import com.pesgard.social_network_gera.ui.theme.AccentStar
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.util.toRelativeTimeString

/**
 * Pantalla de detalle de publicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    serverId: String? = null,
    viewModel: PostDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Cargar post cuando se monta la pantalla
    LaunchedEffect(postId, serverId) {
        if (postId != 0L) {
            // Si tenemos postId local, usarlo directamente (evita duplicados)
            android.util.Log.d("PostDetailScreen", "Cargando post con postId local: $postId")
            viewModel.loadPost(postId, serverId)
        } else if (serverId != null && serverId.isNotBlank()) {
            // Solo si NO tenemos postId local, intentar cargar por serverId
            android.util.Log.d("PostDetailScreen", "Cargando post con serverId: $serverId")
            viewModel.loadPostByServerId(serverId)
        } else {
            // No tenemos ni serverId ni postId válido
            android.util.Log.w("PostDetailScreen", "No se proporcionó serverId ni postId válido")
        }
    }
    
    // Observar el estado del post y cargar comentarios cuando el post esté disponible
    LaunchedEffect(uiState.post?.id) {
        // Cuando el post esté disponible y tenga un ID válido, cargar comentarios
        uiState.post?.let { post ->
            if (post.id != 0L && !uiState.isLoadingComments) {
                // Verificar si ya tenemos comentarios cargados para este post
                val currentPostId = post.id
                val needsCommentLoad = uiState.comments.isEmpty() || 
                    uiState.comments.firstOrNull()?.postId != currentPostId
                
                if (needsCommentLoad) {
                    android.util.Log.d("PostDetailScreen", "Cargando comentarios para post con ID local: $currentPostId")
                    viewModel.loadComments(currentPostId)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Publicación",
                        style = ConnectaTypographyExtensions.appBarTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Más opciones */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.pesgard.social_network_gera.ui.theme.AppBarColor,
                    titleContentColor = com.pesgard.social_network_gera.ui.theme.AppBarTextColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    FullScreenLoadingIndicator()
                }
                uiState.post == null -> {
                    EmptyState(
                        title = "Publicación no encontrada",
                        message = "No se pudo cargar la publicación",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            bottom = 100.dp // Espacio para el input de comentarios
                        )
                    ) {
                        // Post Card
                        item {
                            PostDetailCard(
                                post = uiState.post!!,
                                onLikeClick = { viewModel.likePost() },
                                onDislikeClick = { viewModel.dislikePost() },
                                onFavoriteClick = { viewModel.toggleFavorite() }
                            )
                        }

                        // Divider
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ConnectaSpacing.small)
                                    .padding(horizontal = ConnectaSpacing.medium)
                            )
                        }

                        // Comentarios
                        if (uiState.comments.isEmpty() && !uiState.isLoadingComments) {
                            item {
                                EmptyState(
                                    title = "Sin comentarios",
                                    message = "Sé el primero en comentar",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            items(
                                items = uiState.comments,
                                key = { comment -> comment.id }
                            ) { comment ->
                                CommentItem(
                                    comment = comment,
                                    onLikeClick = {
                                        viewModel.likeComment(comment.id)
                                    },
                                    onReplyClick = {
                                        viewModel.startReply(comment)
                                    },
                                    modifier = Modifier.padding(horizontal = ConnectaSpacing.medium)
                                )
                            }

                            if (uiState.isLoadingComments) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(ConnectaSpacing.large),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FullScreenLoadingIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input de comentarios (sticky bottom)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                CommentInput(
                    text = uiState.commentText,
                    onTextChange = { viewModel.updateCommentText(it) },
                    onSubmit = { viewModel.submitComment() },
                    enabled = uiState.post != null,
                    isSubmitting = uiState.isSubmittingComment,
                    replyingTo = uiState.replyingTo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = ConnectaSpacing.medium)
                )

                // Botón para cancelar respuesta
                if (uiState.replyingTo != null) {
                    androidx.compose.material3.TextButton(
                        onClick = { viewModel.cancelReply() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = ConnectaSpacing.medium)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

/**
 * Card del post en el detalle
 */
@Composable
private fun PostDetailCard(
    post: com.pesgard.social_network_gera.domain.model.Post,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ConnectaSpacing.medium),
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
            // Header: Avatar + Nombre + Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    modifier = Modifier.padding(bottom = ConnectaSpacing.medium)
                )
            }

            // Imágenes
            if (post.images.isNotEmpty()) {
                if (post.images.size == 1) {
                    AsyncImage(
                        model = post.images[0],
                        contentDescription = "Imagen del post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(ConnectaCustomShapes.postImage),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    ImageCarousel(
                        images = post.images,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    )
                }
                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))
            }

            // Stats: Likes y Comentarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.large)
            ) {
                Text(
                    text = "${post.likes} Me gusta",
                    style = ConnectaTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${post.commentsCount} Comentarios",
                    style = ConnectaTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

            // Acciones: Like, Dislike, Favorito
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium)
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

                // Favorito
                IconButton(onClick = onFavoriteClick) {
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
