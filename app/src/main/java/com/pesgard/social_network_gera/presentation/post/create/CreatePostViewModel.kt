package com.pesgard.social_network_gera.presentation.post.create

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.data.local.datastore.SessionManager
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.repository.PostRepository
import com.pesgard.social_network_gera.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Base64
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Estado de la UI para CreatePostScreen
 */
data class CreatePostUiState(
    val title: String = "",
    val description: String = "",
    val selectedImages: List<Uri> = emptyList(),
    val imageBase64List: List<String> = emptyList(), // Imágenes convertidas a base64
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubmitting: Boolean = false
) {
    val canSubmit: Boolean
        get() = title.trim().isNotEmpty() && !isSubmitting
}

/**
 * ViewModel para crear publicaciones
 */
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el título
     */
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    /**
     * Actualiza la descripción
     */
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Agrega una imagen seleccionada
     */
    fun addImage(uri: Uri) {
        _uiState.update { currentState ->
            if (currentState.selectedImages.size < 5) { // Máximo 5 imágenes
                currentState.copy(
                    selectedImages = currentState.selectedImages + uri
                )
            } else {
                currentState.copy(
                    error = "Máximo 5 imágenes permitidas"
                )
            }
        }
    }

    /**
     * Elimina una imagen
     */
    fun removeImage(index: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedImages = currentState.selectedImages.filterIndexed { i, _ -> i != index },
                imageBase64List = currentState.imageBase64List.filterIndexed { i, _ -> i != index }
            )
        }
    }

    /**
     * Convierte una URI a base64
     */
    private suspend fun convertUriToBase64(uri: Uri, context: android.content.Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Comprimir imagen (máximo 1024x1024 para reducir tamaño)
            val maxDimension = 1024
            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val scale = minOf(
                    maxDimension.toFloat() / bitmap.width,
                    maxDimension.toFloat() / bitmap.height
                )
                val scaledWidth = (bitmap.width * scale).toInt()
                val scaledHeight = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
            } else {
                bitmap
            }

            // Convertir a base64
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convierte todas las imágenes seleccionadas a base64
     */
    suspend fun convertImagesToBase64(context: android.content.Context) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val base64List = mutableListOf<String>()
        for (uri in _uiState.value.selectedImages) {
            val base64 = convertUriToBase64(uri, context)
            if (base64 != null) {
                base64List.add(base64)
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error al procesar una imagen"
                    )
                }
                return
            }
        }

        _uiState.update { 
            it.copy(
                imageBase64List = base64List,
                isLoading = false
            )
        }
    }

    /**
     * Crea la publicación
     */
    fun createPost(
        context: android.content.Context,
        onSuccess: () -> Unit
    ) {
        val currentState = _uiState.value
        if (!currentState.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // 1. Convertir imágenes a base64 si hay
            if (currentState.selectedImages.isNotEmpty()) {
                convertImagesToBase64(context)
                if (_uiState.value.error != null) {
                    _uiState.update { it.copy(isSubmitting = false) }
                    return@launch
                }
            }

            // 2. Obtener userId
            val userId = sessionManager.userId.firstOrNull() ?: ""

            // 3. Crear el Post
            val newPost = Post(
                id = 0, // Se asignará al insertar
                userId = userId,
                user = null, // Se cargará desde la DB
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                images = _uiState.value.imageBase64List, // URLs o base64 según el backend
                likes = 0,
                dislikes = 0,
                commentsCount = 0,
                isLiked = false,
                isDisliked = false,
                isFavorite = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                synced = false,
                serverId = null
            )

            // 4. Guardar en repositorio
            val result = postRepository.createPost(newPost)

            _uiState.update { currentState ->
                currentState.copy(
                    isSubmitting = false,
                    error = if (result is Resource.Error) {
                        result.message
                    } else null
                )
            }

            // 5. Si fue exitoso, navegar de vuelta
            if (result is Resource.Success) {
                onSuccess()
            }
        }
    }

    /**
     * Limpia el error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Resetea el formulario
     */
    fun resetForm() {
        _uiState.update { CreatePostUiState() }
    }
}

