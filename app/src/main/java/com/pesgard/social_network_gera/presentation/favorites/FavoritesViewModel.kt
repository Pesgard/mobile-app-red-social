package com.pesgard.social_network_gera.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.repository.PostRepository
import com.pesgard.social_network_gera.util.Resource
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para FavoritesScreen
 */
data class FavoritesUiState(
    val favorites: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel para la pantalla de Favoritos
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    /**
     * Carga las publicaciones favoritas
     */
    fun loadFavorites() {
        viewModelScope.launch {
            postRepository.getFavorites().collect { favorites ->
                _uiState.update { 
                    it.copy(
                        favorites = favorites,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Refresca las publicaciones favoritas
     */
    fun refreshFavorites() {
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
     * Quita un post de favoritos
     */
    fun unfavoritePost(postId: Long) {
        viewModelScope.launch {
            postRepository.unfavoritePost(postId)
            // El estado se actualizará automáticamente a través del Flow de getFavorites()
        }
    }

    /**
     * Da like a un post
     */
    fun likePost(postId: Long) {
        viewModelScope.launch {
            postRepository.likePost(postId)
            // El estado se actualizará automáticamente a través del Flow de getFavorites()
        }
    }

    /**
     * Da dislike a un post
     */
    fun dislikePost(postId: Long) {
        viewModelScope.launch {
            postRepository.dislikePost(postId)
            // El estado se actualizará automáticamente a través del Flow de getFavorites()
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
