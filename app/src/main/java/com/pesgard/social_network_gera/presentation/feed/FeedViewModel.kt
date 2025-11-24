package com.pesgard.social_network_gera.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.domain.repository.PostRepository
import com.pesgard.social_network_gera.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para FeedScreen
 */
data class FeedUiState(
    val posts: List<com.pesgard.social_network_gera.domain.model.Post> = emptyList(),
    val filteredPosts: List<com.pesgard.social_network_gera.domain.model.Post> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel para la pantalla de Feed
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    private var isPostsFlowActive = false

    init {
        // Cargar posts desde el servidor al iniciar
        initialLoad()
    }

    /**
     * Carga inicial: primero refresca desde el servidor, luego observa cambios
     */
    private fun initialLoad() {
        viewModelScope.launch {
            // Primero refrescar desde el servidor
            val refreshResult = postRepository.refreshPosts()
            
            // Iniciar observación de posts (solo una vez)
            if (!isPostsFlowActive) {
                isPostsFlowActive = true
                loadPosts()
            }
            
            // Actualizar estado después de la carga inicial
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    error = if (refreshResult is Resource.Error) {
                        refreshResult.message
                    } else null
                )
            }
        }
    }

    /**
     * Carga las publicaciones
     */
    fun loadPosts() {
        viewModelScope.launch {
            postRepository.getPosts().collect { posts ->
                _uiState.update { currentState ->
                    val filtered = if (currentState.searchQuery.isNotEmpty()) {
                        filterPosts(posts, currentState.searchQuery)
                    } else {
                        posts
                    }
                    currentState.copy(
                        posts = posts,
                        filteredPosts = filtered
                        // No actualizar isLoading aquí, se actualiza en initialLoad()
                    )
                }
            }
        }
    }

    /**
     * Refresca las publicaciones desde el servidor
     */
    fun refreshPosts() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            val result = postRepository.refreshPosts()
            _uiState.update { currentState ->
                currentState.copy(
                    isRefreshing = false,
                    error = if (result is Resource.Error) {
                        result.message
                    } else null
                )
            }
        }
    }

    /**
     * Busca publicaciones por query
     */
    fun searchPosts(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isNotEmpty()) {
                filterPosts(currentState.posts, query)
            } else {
                currentState.posts
            }
            currentState.copy(
                searchQuery = query,
                filteredPosts = filtered
            )
        }
    }

    /**
     * Filtra posts por query (título o descripción)
     */
    private fun filterPosts(
        posts: List<com.pesgard.social_network_gera.domain.model.Post>,
        query: String
    ): List<com.pesgard.social_network_gera.domain.model.Post> {
        val lowerQuery = query.lowercase()
        return posts.filter { post ->
            post.title.lowercase().contains(lowerQuery) ||
            post.description.lowercase().contains(lowerQuery) ||
            post.user?.alias?.lowercase()?.contains(lowerQuery) == true
        }
    }

    /**
     * Da like a un post
     */
    fun likePost(postId: Long) {
        viewModelScope.launch {
            postRepository.likePost(postId)
            // El estado se actualizará automáticamente a través del Flow de getPosts()
        }
    }

    /**
     * Da dislike a un post
     */
    fun dislikePost(postId: Long) {
        viewModelScope.launch {
            postRepository.dislikePost(postId)
            // El estado se actualizará automáticamente a través del Flow de getPosts()
        }
    }

    /**
     * Agrega/quita de favoritos
     */
    fun toggleFavorite(postId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            if (isFavorite) {
                postRepository.unfavoritePost(postId)
            } else {
                postRepository.favoritePost(postId)
            }
            // El estado se actualizará automáticamente a través del Flow de getPosts()
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
