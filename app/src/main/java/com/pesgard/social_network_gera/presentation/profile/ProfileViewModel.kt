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
    val favorites: List<Post> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.POSTS,
    val isLoading: Boolean = true,
    val isLoadingPosts: Boolean = false,
    val isLoadingFavorites: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

/**
 * Tabs del perfil
 */
enum class ProfileTab {
    POSTS,
    FAVORITES
}

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
    
    // Jobs para poder cancelarlos si es necesario
    private var postsJob: kotlinx.coroutines.Job? = null
    private var favoritesJob: kotlinx.coroutines.Job? = null
    private var userJob: kotlinx.coroutines.Job? = null

    init {
        loadProfile()
    }

    /**
     * Carga el perfil del usuario actual
     * Solo se ejecuta una vez al inicializar
     */
    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        userJob?.cancel()
        userJob = viewModelScope.launch {
            // Primero refrescar el perfil desde el servidor
            val refreshResult = authRepository.refreshUserProfile()
            if (refreshResult is Resource.Error) {
                android.util.Log.w("ProfileViewModel", "Error refrescando perfil: ${refreshResult.message}")
            }
            
            // Obtener usuario actual (solo una vez)
            val user = authRepository.getCurrentUser().firstOrNull()
            
            _uiState.update { currentState ->
                currentState.copy(
                    user = user,
                    isLoading = false,
                    error = if (refreshResult is Resource.Error && user == null) {
                        refreshResult.message
                    } else null
                )
            }
            
            // Cargar posts del usuario si existe y el tab actual es POSTS
            user?.let {
                if (_uiState.value.selectedTab == ProfileTab.POSTS) {
                    loadUserPosts(it.id, refreshFromServer = true)
                }
            }
        }
    }

    /**
     * Carga los posts del usuario
     * @param userId ID del usuario
     * @param refreshFromServer Si true, refresca desde el servidor primero
     */
    private fun loadUserPosts(userId: String, refreshFromServer: Boolean = false) {
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPosts = true) }
            
            // Refrescar desde el servidor si se solicita
            if (refreshFromServer) {
                postRepository.refreshUserPosts(userId).let { result ->
                    if (result is Resource.Error) {
                        android.util.Log.w("ProfileViewModel", "Error refrescando posts del usuario: ${result.message}")
                    }
                }
            }
            
            // Obtener posts una sola vez (no observar continuamente)
            val posts = postRepository.getPostsByUserId(userId).firstOrNull() ?: emptyList()
            
            _uiState.update { currentState ->
                currentState.copy(
                    posts = posts,
                    isLoadingPosts = false
                )
            }
        }
    }
    
    /**
     * Carga los favoritos del usuario
     * @param refreshFromServer Si true, refresca desde el servidor primero
     */
    private fun loadFavorites(refreshFromServer: Boolean = false) {
        favoritesJob?.cancel()
        favoritesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFavorites = true) }
            
            // Refrescar desde el servidor si se solicita
            if (refreshFromServer) {
                postRepository.refreshFavorites().let { result ->
                    if (result is Resource.Error) {
                        android.util.Log.w("ProfileViewModel", "Error refrescando favoritos: ${result.message}")
                    }
                }
            }
            
            // Obtener favoritos una sola vez (no observar continuamente)
            val favorites = postRepository.getFavorites().firstOrNull() ?: emptyList()
            
            _uiState.update { currentState ->
                currentState.copy(
                    favorites = favorites,
                    isLoadingFavorites = false
                )
            }
        }
    }
    
    /**
     * Cambia el tab seleccionado y carga los datos correspondientes
     */
    fun selectTab(tab: ProfileTab) {
        val currentTab = _uiState.value.selectedTab
        if (currentTab == tab) {
            // Ya está seleccionado este tab, no hacer nada
            return
        }
        
        _uiState.update { it.copy(selectedTab = tab) }
        
        // Cargar datos del tab seleccionado
        val user = _uiState.value.user
        user?.let {
            when (tab) {
                ProfileTab.POSTS -> {
                    loadUserPosts(it.id, refreshFromServer = true)
                }
                ProfileTab.FAVORITES -> {
                    loadFavorites(refreshFromServer = true)
                }
            }
        }
    }

    /**
     * Refresca el perfil y los datos del tab actual desde el servidor
     */
    fun refreshProfile() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            // Refrescar perfil del usuario
            val profileResult = authRepository.refreshUserProfile()
            
            val user = _uiState.value.user
            if (user != null) {
                // Refrescar datos según el tab actual
                when (_uiState.value.selectedTab) {
                    ProfileTab.POSTS -> {
                        val userPostsResult = postRepository.refreshUserPosts(user.id)
                        _uiState.update { currentState ->
                            currentState.copy(
                                isRefreshing = false,
                                error = when {
                                    profileResult is Resource.Error -> profileResult.message
                                    userPostsResult is Resource.Error -> userPostsResult.message
                                    else -> null
                                }
                            )
                        }
                        // Recargar posts
                        loadUserPosts(user.id, refreshFromServer = false)
                    }
                    ProfileTab.FAVORITES -> {
                        val favoritesResult = postRepository.refreshFavorites()
                        _uiState.update { currentState ->
                            currentState.copy(
                                isRefreshing = false,
                                error = when {
                                    profileResult is Resource.Error -> profileResult.message
                                    favoritesResult is Resource.Error -> favoritesResult.message
                                    else -> null
                                }
                            )
                        }
                        // Recargar favoritos
                        loadFavorites(refreshFromServer = false)
                    }
                }
            } else {
                _uiState.update { currentState ->
                    currentState.copy(
                        isRefreshing = false,
                        error = (profileResult as? Resource.Error)?.message                    )
                }
            }
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
     * Elimina un post
     * @param postId ID local del post a eliminar
     */
    fun deletePost(postId: Long) {
        viewModelScope.launch {
            val result = postRepository.deletePost(postId)
            if (result is Resource.Error) {
                _uiState.update { it.copy(error = result.message) }
            } else {
                // Recargar posts después de eliminar
                _uiState.value.user?.let {
                    loadUserPosts(it.id, refreshFromServer = false)
                }
            }
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
