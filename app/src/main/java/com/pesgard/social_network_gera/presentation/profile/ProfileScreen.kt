package com.pesgard.social_network_gera.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.ui.components.EmptyState
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.PostCard
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.util.toRelativeTimeString

/**
 * Pantalla de Perfil del Usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPostDetail: (Long, String?) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToEditPost: (Long) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var postToDelete by remember { mutableStateOf<com.pesgard.social_network_gera.domain.model.Post?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil",
                        style = ConnectaTypographyExtensions.appBarTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de refresh
                    IconButton(
                        onClick = { viewModel.refreshProfile() },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Botón de configuración
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
                uiState.user == null -> {
                    EmptyState(
                        title = "No se pudo cargar el perfil",
                        message = "Por favor, inicia sesión nuevamente",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            bottom = ConnectaSpacing.medium
                        )
                    ) {
                        // Header del perfil
                        item {
                            ProfileHeader(
                                user = uiState.user!!,
                                postsCount = uiState.posts.size,
                                favoritesCount = uiState.favorites.size,
                                onEditClick = onNavigateToEditProfile,
                                onLogoutClick = {
                                    viewModel.logout(onLogout)
                                }
                            )
                        }

                        // Divider
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ConnectaSpacing.small)
                            )
                        }

                        // Tabs
                        item {
                            ProfileTabs(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        // Contenido según el tab seleccionado
                        when (uiState.selectedTab) {
                            ProfileTab.POSTS -> {
                                // Posts del usuario
                                if (uiState.posts.isEmpty() && !uiState.isLoadingPosts) {
                                    item {
                                        EmptyState(
                                            title = "No hay publicaciones",
                                            message = "Crea tu primera publicación",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    items(
                                        items = uiState.posts,
                                        key = { post -> post.id }
                                    ) { post ->
                                        // Verificar si el usuario actual es el autor del post
                                        val isAuthor = uiState.user?.id == post.userId
                                        
                                        PostCardWithMenu(
                                            post = post,
                                            isAuthor = isAuthor,
                                            onPostClick = {
                                                onNavigateToPostDetail(post.id, post.serverId)
                                            },
                                            onLikeClick = {
                                                // Los likes se manejan desde el PostCard
                                            },
                                            onDislikeClick = {
                                                // Los dislikes se manejan desde el PostCard
                                            },
                                            onFavoriteClick = {
                                                // Los favoritos se manejan desde el PostCard
                                            },
                                            onCommentClick = {
                                                onNavigateToPostDetail(post.id, post.serverId)
                                            },
                                            onEditClick = {
                                                onNavigateToEditPost(post.id)
                                            },
                                            onDeleteClick = {
                                                postToDelete = post
                                                showDeleteDialog = true
                                            },
                                            modifier = Modifier.padding(horizontal = ConnectaSpacing.medium)
                                        )
                                    }

                                    if (uiState.isLoadingPosts) {
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
                            ProfileTab.FAVORITES -> {
                                // Favoritos del usuario
                                if (uiState.favorites.isEmpty() && !uiState.isLoadingFavorites) {
                                    item {
                                        EmptyState(
                                            title = "No hay favoritos",
                                            message = "Guarda publicaciones que te gusten",
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                } else {
                                    items(
                                        items = uiState.favorites,
                                        key = { post -> post.id }
                                    ) { post ->
                                        PostCard(
                                            post = post,
                                            onPostClick = {
                                                onNavigateToPostDetail(post.id, post.serverId)
                                            },
                                            onLikeClick = {
                                                // Los likes se manejan desde el PostCard
                                            },
                                            onDislikeClick = {
                                                // Los dislikes se manejan desde el PostCard
                                            },
                                            onFavoriteClick = {
                                                // Los favoritos se manejan desde el PostCard
                                            },
                                            onCommentClick = {
                                                onNavigateToPostDetail(post.id, post.serverId)
                                            },
                                            modifier = Modifier.padding(horizontal = ConnectaSpacing.medium)
                                        )
                                    }

                                    if (uiState.isLoadingFavorites) {
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
                }
            }

            // Mostrar error si existe
            if (uiState.error != null) {
                LaunchedEffect(uiState.error) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
            }
        }
        
        // Diálogo de confirmación para eliminar post
        if (showDeleteDialog && postToDelete != null) {
            DeletePostDialog(
                post = postToDelete!!,
                onConfirm = {
                    viewModel.deletePost(postToDelete!!.id)
                    postToDelete = null
                    showDeleteDialog = false
                },
                onDismiss = {
                    postToDelete = null
                    showDeleteDialog = false
                }
            )
        }
    }
}

/**
 * PostCard con menú de opciones para editar/eliminar
 */
@Composable
private fun PostCardWithMenu(
    post: com.pesgard.social_network_gera.domain.model.Post,
    isAuthor: Boolean,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Crear un Card personalizado con el menú integrado
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPostClick() },
        shape = com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes.card,
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

                // Botón de más opciones con menú (solo si es autor)
                if (isAuthor) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(ConnectaDimensions.iconMedium)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Más opciones",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Eliminar",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
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
                if (post.images.size == 1) {
                    AsyncImage(
                        model = post.images[0],
                        contentDescription = "Imagen del post",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes.postImage),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    com.pesgard.social_network_gera.ui.components.ImageCarousel(
                        images = post.images,
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
                            tint = if (post.isLiked) com.pesgard.social_network_gera.ui.theme.AccentLike else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ConnectaDimensions.iconMedium)
                        )
                        Text(
                            text = "${post.likes}",
                            style = ConnectaTypography.bodyMedium,
                            color = if (post.isLiked) com.pesgard.social_network_gera.ui.theme.AccentLike else MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = if (post.isDisliked) com.pesgard.social_network_gera.ui.theme.AccentLike else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(ConnectaDimensions.iconMedium)
                        )
                        Text(
                            text = "${post.dislikes}",
                            style = ConnectaTypography.bodyMedium,
                            color = if (post.isDisliked) com.pesgard.social_network_gera.ui.theme.AccentLike else MaterialTheme.colorScheme.onSurfaceVariant
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
                        tint = if (post.isFavorite) com.pesgard.social_network_gera.ui.theme.AccentStar else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación para eliminar post
 */
@Composable
private fun DeletePostDialog(
    post: com.pesgard.social_network_gera.domain.model.Post,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminar publicación")
        },
        text = {
            Text("¿Estás seguro de que deseas eliminar esta publicación? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    "Eliminar",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Tabs del perfil
 */
@Composable
private fun ProfileTabs(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        modifier = modifier.fillMaxWidth()
    ) {
        Tab(
            selected = selectedTab == ProfileTab.POSTS,
            onClick = { onTabSelected(ProfileTab.POSTS) },
            text = { Text("Publicaciones") }
        )
        Tab(
            selected = selectedTab == ProfileTab.FAVORITES,
            onClick = { onTabSelected(ProfileTab.FAVORITES) },
            text = { Text("Favoritos") }
        )
    }
}

/**
 * Header del perfil con información del usuario
 */
@Composable
private fun ProfileHeader(
    user: com.pesgard.social_network_gera.domain.model.User,
    postsCount: Int,
    favoritesCount: Int,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ConnectaSpacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ConnectaSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            AsyncImage(
                model = user.avatarUrl ?: "",
                contentDescription = "Avatar de ${user.alias}",
                modifier = Modifier
                    .size(ConnectaDimensions.avatarLarge)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

            // Nombre completo
            Text(
                text = user.fullName,
                style = ConnectaTypographyExtensions.appBarTitle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Alias
            Text(
                text = "@${user.alias}",
                style = ConnectaTypographyExtensions.username,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$postsCount",
                        style = ConnectaTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Publicaciones",
                        style = ConnectaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$favoritesCount",
                        style = ConnectaTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Favoritos",
                        style = ConnectaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(ConnectaSpacing.large))

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium)
            ) {
                // Botón de editar perfil
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(ConnectaSpacing.small))
                    Text("Editar Perfil")
                }

                // Botón de cerrar sesión
                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(ConnectaSpacing.small))
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}
