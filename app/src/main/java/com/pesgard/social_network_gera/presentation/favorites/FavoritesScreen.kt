package com.pesgard.social_network_gera.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.ui.components.EmptyState
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.PostCard
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions

/**
 * Pantalla de Favoritos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToPostDetail: (Long, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favoritos",
                        style = ConnectaTypographyExtensions.appBarTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de refresh
                    IconButton(
                        onClick = { viewModel.refreshFavorites() },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
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
                uiState.favorites.isEmpty() -> {
                    EmptyState(
                        title = "No tienes favoritos",
                        message = "Marca publicaciones como favoritas para verlas aquí",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = ConnectaSpacing.medium,
                            vertical = ConnectaSpacing.medium
                        )
                    ) {
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
                                    viewModel.likePost(post.id)
                                },
                                onDislikeClick = {
                                    viewModel.dislikePost(post.id)
                                },
                                onFavoriteClick = {
                                    // Quitar de favoritos
                                    viewModel.unfavoritePost(post.id)
                                },
                                onCommentClick = {
                                    onNavigateToPostDetail(post.id, post.serverId)
                                }
                            )
                        }
                    }
                }
            }

            // Mostrar error si existe
            if (uiState.error != null) {
                LaunchedEffect(uiState.error) {
                    // El error se mostrará en un Snackbar
                    // Por ahora, solo lo limpiamos después de un tiempo
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearError()
                }
            }
        }
    }
}
