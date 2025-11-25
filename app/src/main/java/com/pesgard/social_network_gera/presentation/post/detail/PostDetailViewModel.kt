package com.pesgard.social_network_gera.presentation.post.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.repository.CommentRepository
import com.pesgard.social_network_gera.domain.repository.PostRepository
import com.pesgard.social_network_gera.data.local.datastore.SessionManager
import com.pesgard.social_network_gera.util.NetworkMonitor
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
 * Estado de la UI para PostDetailScreen
 */
data class PostDetailUiState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingComments: Boolean = false,
    val error: String? = null,
    val commentText: String = "",
    val replyingTo: Comment? = null, // Comentario al que se está respondiendo
    val isSubmittingComment: Boolean = false
)

/**
 * ViewModel para la pantalla de detalle de publicación
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val sessionManager: SessionManager,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    /**
     * Carga el post por su ID local
     * @param postId ID local del post
     * @param serverId ID del servidor (opcional, para cargar desde servidor si no existe localmente)
     * Si el post tiene serverId y hay conexión, refresca desde el servidor para obtener comentarios actualizados
     * Si el post no existe localmente y se proporciona serverId, lo carga desde el servidor
     */
    fun loadPost(postId: Long, serverId: String? = null) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Primero obtener el post de la base de datos local
            var firstPost: Post? = null
            var hasTriedServerLoad = false
            
            // Primero verificar si el post existe localmente
            val localPost = postRepository.getPostById(postId).firstOrNull()
            
            if (localPost != null) {
                // El post existe localmente, observar cambios y refrescar si es necesario
                firstPost = localPost
                
                // DESHABILITADO: Esto causa posts duplicados y pérdida de comentarios locales
                // Si el post tiene serverId y hay conexión, refrescar desde el servidor
                // para obtener los comentarios más recientes
                /*
                if (!localPost.serverId.isNullOrEmpty() && networkMonitor.isOnline()) {
                    postRepository.refreshPostById(localPost.serverId).let { result ->
                        if (result is Resource.Error) {
                            android.util.Log.w("PostDetailViewModel", "Error refrescando post: ${result.message}")
                        } else {
                            android.util.Log.d("PostDetailViewModel", "Post refrescado exitosamente desde servidor")
                        }
                    }
                }
                */
                
                // Observar cambios en el post
                postRepository.getPostById(postId).collect { post ->
                    if (post != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                post = post,
                                isLoading = false,
                                error = null
                            )
                        }
                        // Cargar comentarios automáticamente cuando el post esté disponible
                        if (post.id != 0L) {
                            loadCommentsInternal(post.id)
                        }
                    }
                }
            } else {
                // El post no existe localmente
                if (serverId != null && networkMonitor.isOnline()) {
                    // Si tenemos serverId, usar loadPostByServerId que maneja mejor este caso
                    android.util.Log.d("PostDetailViewModel", "Post no encontrado localmente, usando loadPostByServerId con serverId: $serverId")
                    // Cambiar a usar loadPostByServerId que está mejor optimizado para este caso
                    loadPostByServerId(serverId)
                } else {
                    // No tenemos serverId o no hay conexión
                    _uiState.update { currentState ->
                        currentState.copy(
                            post = null,
                            isLoading = false,
                            error = if (serverId == null) {
                                "Publicación no encontrada"
                            } else {
                                "Publicación no encontrada. Verifica tu conexión."
                            }
                        )
                    }
                }
            }
        }
    }

    // Job para la carga de comentarios, para poder cancelarla si es necesario
    private var commentsJob: kotlinx.coroutines.Job? = null
    
    /**
     * Carga los comentarios del post usando el postId local
     */
    fun loadComments(postId: Long) {
        // Cancelar la carga anterior de comentarios si existe
        commentsJob?.cancel()
        
        if (postId == 0L) {
            android.util.Log.w("PostDetailViewModel", "loadComments llamado con postId 0, esperando a que el post se cargue...")
            return
        }
        
        loadCommentsInternal(postId)
    }
    
    /**
     * Carga los comentarios internamente usando el postId
     */
    private fun loadCommentsInternal(postId: Long) {
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingComments = true) }
            
            android.util.Log.d("PostDetailViewModel", "Cargando comentarios para postId: $postId")
            commentRepository.getCommentsByPostId(postId).collect { comments ->
                android.util.Log.d("PostDetailViewModel", "Comentarios recibidos: ${comments.size} para postId: $postId")
                comments.forEach { comment ->
                    android.util.Log.d("PostDetailViewModel", "  - Comentario ID: ${comment.id}, postId: ${comment.postId}, texto: ${comment.text.take(50)}")
                }
                _uiState.update { currentState ->
                    currentState.copy(
                        comments = comments,
                        isLoadingComments = false
                    )
                }
            }
        }
    }

    /**
     * Da like al post
     */
    fun likePost() {
        val post = _uiState.value.post ?: return
        viewModelScope.launch {
            postRepository.likePost(post.id)
            // El estado se actualizará automáticamente a través del Flow
        }
    }

    /**
     * Da dislike al post
     */
    fun dislikePost() {
        val post = _uiState.value.post ?: return
        viewModelScope.launch {
            postRepository.dislikePost(post.id)
            // El estado se actualizará automáticamente a través del Flow
        }
    }

    /**
     * Agrega/quita de favoritos
     */
    fun toggleFavorite() {
        val post = _uiState.value.post ?: return
        viewModelScope.launch {
            postRepository.favoritePost(post.id)
            // El estado se actualizará automáticamente a través del Flow
        }
    }

    /**
     * Actualiza el texto del comentario
     */
    fun updateCommentText(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    /**
     * Inicia una respuesta a un comentario
     */
    fun startReply(comment: Comment) {
        _uiState.update { 
            it.copy(
                replyingTo = comment,
                commentText = "@${comment.user?.alias ?: "usuario"} "
            )
        }
    }

    /**
     * Cancela la respuesta
     */
    fun cancelReply() {
        _uiState.update { 
            it.copy(
                replyingTo = null,
                commentText = ""
            )
        }
    }

    /**
     * Envía un comentario o respuesta
     */
    fun submitComment() {
        val post = _uiState.value.post ?: return
        val text = _uiState.value.commentText.trim()
        val replyingTo = _uiState.value.replyingTo

        if (text.isEmpty()) return

        _uiState.update { it.copy(isSubmittingComment = true, error = null) }

        viewModelScope.launch {
            // Obtener userId actual
            val userId = sessionManager.userId.firstOrNull() ?: ""

            // Crear el Comment
            val newComment = Comment(
                id = 0, // Se asignará al insertar
                postId = post.id,
                userId = userId,
                user = null, // Se cargará desde la DB
                parentCommentId = replyingTo?.id,
                replies = emptyList(),
                text = text,
                likes = 0,
                createdAt = System.currentTimeMillis(),
                synced = false,
                serverId = null
            )

            android.util.Log.d("PostDetailViewModel", "Creando comentario para postId: ${post.id}, userId: $userId, texto: $text")

            val result = if (replyingTo != null) {
                // Es una respuesta
                commentRepository.replyToComment(
                    parentCommentId = replyingTo.id,
                    comment = newComment
                )
            } else {
                // Es un comentario nuevo
                commentRepository.createComment(newComment)
            }

            _uiState.update { currentState ->
                currentState.copy(
                    isSubmittingComment = false,
                    commentText = "",
                    replyingTo = null,
                    error = if (result is Resource.Error) {
                        result.message
                    } else null
                )
            }

            // Recargar comentarios después de crear uno nuevo
            if (result is Resource.Success) {
                // Sincronizar comentarios pendientes antes de recargar
                commentRepository.syncComments()
                loadComments(post.id)
            }
        }
    }

    /**
     * Da like a un comentario
     */
    fun likeComment(commentId: Long) {
        viewModelScope.launch {
            commentRepository.likeComment(commentId)
            // El estado se actualizará automáticamente a través del Flow
        }
    }

    /**
     * Carga el post directamente desde el servidor usando su serverId
     */
    fun loadPostByServerId(serverId: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Primero verificar si el post ya existe localmente
            val existingPost = postRepository.getPostByServerId(serverId).firstOrNull()
            
            if (existingPost != null) {
                // El post ya existe localmente
                _uiState.update { currentState ->
                    currentState.copy(
                        post = existingPost,
                        isLoading = false,
                        error = null
                    )
                }
                // Cargar comentarios del post local
                loadCommentsInternal(existingPost.id)
                
                // Refrescar desde el servidor para obtener los comentarios más recientes
                if (networkMonitor.isOnline()) {
                    postRepository.refreshPostById(serverId).let { result ->
                        if (result is Resource.Success) {
                            android.util.Log.d("PostDetailViewModel", "Post refrescado exitosamente desde servidor")
                        } else {
                            android.util.Log.w("PostDetailViewModel", "Error refrescando post: ${(result as? Resource.Error)?.message}")
                        }
                    }
                }
                
                // Observar cambios en el post después del refresh
                postRepository.getPostByServerId(serverId).collect { post ->
                    if (post != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                post = post,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                }
            } else {
                // El post no existe localmente
                if (!networkMonitor.isOnline()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Sin conexión. No se puede cargar la publicación."
                        )
                    }
                    return@launch
                }
                
                // Cargar desde el servidor
                android.util.Log.d("PostDetailViewModel", "Cargando post desde servidor con serverId: $serverId")
                val refreshResult = postRepository.refreshPostById(serverId)
                
                if (refreshResult is Resource.Success) {
                    // Observar el post después de cargarlo desde el servidor
                    postRepository.getPostByServerId(serverId).collect { post ->
                        if (post != null) {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    post = post,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            // Cargar comentarios después de que el post esté guardado
                            loadCommentsInternal(post.id)
                        }
                    }
                } else {
                    val errorMessage = (refreshResult as? Resource.Error)?.message ?: "Error desconocido"
                    android.util.Log.e("PostDetailViewModel", "Error cargando post desde servidor: $errorMessage")
                    _uiState.update { currentState ->
                        currentState.copy(
                            post = null,
                            isLoading = false,
                            error = "No se pudo cargar la publicación: $errorMessage"
                        )
                    }
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
