package com.pesgard.social_network_gera.presentation.post.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Estado de la UI para EditPostScreen
 */
data class EditPostUiState(
    val post: Post? = null,
    val title: String = "",
    val description: String = "",
    val existingImages: List<String> = emptyList(), // URLs de imágenes existentes
    val selectedImages: List<Uri> = emptyList(), // Nuevas imágenes seleccionadas
    val imageBase64List: List<String> = emptyList(), // Imágenes nuevas convertidas a base64
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSubmitting: Boolean = false
) {
    val canSubmit: Boolean
        get() = title.trim().isNotEmpty() && !isSubmitting && !isLoading
}

/**
 * ViewModel para editar publicaciones
 */
@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditPostUiState())
    val uiState: StateFlow<EditPostUiState> = _uiState.asStateFlow()

    /**
     * Carga el post a editar
     */
    fun loadPost(postId: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            postRepository.getPostById(postId).collect { post ->
                if (post != null) {
                    _uiState.update {
                        it.copy(
                            post = post,
                            title = post.title,
                            description = post.description,
                            existingImages = post.images,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Publicación no encontrada"
                        )
                    }
                }
            }
        }
    }

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
            val totalImages = currentState.existingImages.size + currentState.selectedImages.size
            if (totalImages < 5) {
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
     * Elimina una imagen existente
     */
    fun removeExistingImage(index: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                existingImages = currentState.existingImages.filterIndexed { i, _ -> i != index }
            )
        }
    }

    /**
     * Elimina una imagen nueva seleccionada
     */
    fun removeSelectedImage(index: Int) {
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
     * Actualiza la publicación
     */
    fun updatePost(
        context: android.content.Context,
        onSuccess: () -> Unit
    ) {
        val currentState = _uiState.value
        val post = currentState.post ?: return
        if (!currentState.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // 1. Convertir imágenes nuevas a base64 si hay
            if (currentState.selectedImages.isNotEmpty()) {
                convertImagesToBase64(context)
                if (_uiState.value.error != null) {
                    _uiState.update { it.copy(isSubmitting = false) }
                    return@launch
                }
            }

            // 2. Combinar imágenes existentes con nuevas
            val allImages = currentState.existingImages + _uiState.value.imageBase64List

            // 3. Actualizar el Post
            val updatedPost = post.copy(
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                images = allImages,
                updatedAt = System.currentTimeMillis()
            )

            // 4. Guardar en repositorio
            val result = postRepository.updatePost(updatedPost)

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
}

