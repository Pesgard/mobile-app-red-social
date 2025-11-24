package com.pesgard.social_network_gera.presentation.profile

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
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
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.ui.components.EmptyState
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.PostCard
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions

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
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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
    }
}

/**
 * Header del perfil con información del usuario
 */
@Composable
private fun ProfileHeader(
    user: com.pesgard.social_network_gera.domain.model.User,
    postsCount: Int,
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
