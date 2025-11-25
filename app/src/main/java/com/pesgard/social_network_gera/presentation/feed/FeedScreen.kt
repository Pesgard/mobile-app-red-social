package com.pesgard.social_network_gera.presentation.feed

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesgard.social_network_gera.ui.components.EmptyState
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.PostCard
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.ui.theme.Primary

/**
 * Pantalla de Feed de Publicaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNavigateToPostDetail: (Long, String?) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEditPost: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<com.pesgard.social_network_gera.domain.model.Post?>(null) }

    // Actualizar búsqueda cuando cambia el query
    LaunchedEffect(searchQuery) {
        viewModel.searchPosts(searchQuery)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MeetBand",
                        style = ConnectaTypographyExtensions.appBarTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Botón de refresh
                    IconButton(
                        onClick = { viewModel.refreshPosts() },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Botón de búsqueda
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = com.pesgard.social_network_gera.ui.theme.AppBarTextColor
                        )
                    }
                    // Botón de crear post
                    IconButton(onClick = onNavigateToCreatePost) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crear publicación",
                            tint = com.pesgard.social_network_gera.ui.theme.AppBarTextColor
                        )
                    }
                    // Botón de perfil
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = com.pesgard.social_network_gera.ui.theme.AppBarTextColor
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda (condicional)
            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar publicaciones...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ConnectaSpacing.medium),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                )
            }

            // Contenido principal
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        FullScreenLoadingIndicator()
                    }
                    uiState.filteredPosts.isEmpty() && uiState.searchQuery.isEmpty() -> {
                        EmptyState(
                            title = "No hay publicaciones",
                            message = "Sé el primero en compartir algo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    uiState.filteredPosts.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                        EmptyState(
                            title = "No se encontraron resultados",
                            message = "Intenta con otros términos de búsqueda",
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
                                items = uiState.filteredPosts,
                                key = { post -> post.id }
                            ) { post ->
                                val isAuthor = uiState.currentUserId == post.userId
                                
                                if (isAuthor) {
                                    // Si es el autor, usar componente del Profile con menú
                                    com.pesgard.social_network_gera.presentation.profile.PostCardWithMenu(
                                        post = post,
                                        isAuthor = isAuthor,
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
                                            viewModel.toggleFavorite(post.id, post.isFavorite)
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
                                        }
                                    )
                                } else {
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
                                            viewModel.toggleFavorite(post.id, post.isFavorite)
                                        },
                                        onCommentClick = {
                                            onNavigateToPostDetail(post.id, post.serverId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Mostrar error si existe (usando Snackbar simple)
                if (uiState.error != null) {
                    LaunchedEffect(uiState.error) {
                        // El error se mostrará en un Snackbar
                        // Por ahora, solo lo limpiamos después de un tiempo
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearError()
                    }
                }
            }
            
            // Diálogo de confirmación de eliminación
            if (showDeleteDialog && postToDelete != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        postToDelete = null
                    },
                    title = { Text("Eliminar publicación") },
                    text = { Text("¿Estás seguro de que deseas eliminar esta publicación? Esta acción no se puede deshacer.") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                viewModel.deletePost(postToDelete!!.id)
                                showDeleteDialog = false
                                postToDelete = null
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                showDeleteDialog = false
                                postToDelete = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
