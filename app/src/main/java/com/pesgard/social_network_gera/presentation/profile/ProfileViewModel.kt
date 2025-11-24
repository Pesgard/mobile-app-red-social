package com.pesgard.social_network_gera.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.data.local.AppDatabase
import com.pesgard.social_network_gera.data.local.AppDatabase_Impl
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.model.User
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.domain.repository.PostRepository
import com.pesgard.social_network_gera.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para ProfileScreen
 */
data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingPosts: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * ViewModel para la pantalla de Perfil
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Carga el perfil del usuario actual
     * Primero intenta refrescar desde el servidor, luego observa cambios locales
     */
    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Primero refrescar el perfil desde el servidor
            val refreshResult = authRepository.refreshUserProfile()
            if (refreshResult is Resource.Error) {
                android.util.Log.w("ProfileViewModel", "Error refrescando perfil: ${refreshResult.message}")
                // Continuar con datos locales aunque falle el refresh
            }
            
            // Observar cambios en el usuario actual
            authRepository.getCurrentUser().collect { user ->
                _uiState.update { currentState ->
                    currentState.copy(
                        user = user,
                        isLoading = false,
                        error = if (refreshResult is Resource.Error && user == null) {
                            refreshResult.message
                        } else null
                    )
                }
                
                // Cargar posts del usuario si existe
                user?.let { loadUserPosts(it.id) }
            }
        }
    }

    /**
     * Carga los posts del usuario
     */
    private fun loadUserPosts(userId: String) {
        _uiState.update { it.copy(isLoadingPosts = true) }
        viewModelScope.launch {
            postRepository.getPostsByUserId(userId).collect { posts ->
                _uiState.update { currentState ->
                    currentState.copy(
                        posts = posts,
                        isLoadingPosts = false
                    )
                }
            }
        }
    }

    /**
     * Refresca el perfil y los posts desde el servidor
     */
    fun refreshProfile() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            // Refrescar perfil del usuario
            val profileResult = authRepository.refreshUserProfile()
            
            // Refrescar posts
            val postsResult = postRepository.refreshPosts()
            
            _uiState.update { currentState ->
                currentState.copy(
                    isRefreshing = false,
                    error = when {
                        profileResult is Resource.Error -> profileResult.message
                        postsResult is Resource.Error -> postsResult.message
                        else -> null
                    }
                )
            }
            
            // Recargar posts del usuario
            _uiState.value.user?.let { loadUserPosts(it.id) }
        }
    }

    /**
     * Cierra la sesión
     */
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
