package com.pesgard.social_network_gera.presentation.profile.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.domain.model.User
import com.pesgard.social_network_gera.domain.repository.AuthRepository
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
 * Estado de la UI para EditProfileScreen
 */
data class EditProfileUiState(
    val user: User? = null,
    val alias: String = "",
    val phone: String = "",
    val address: String = "",
    val avatarUrl: String = "",
    val selectedAvatarUri: Uri? = null,
    val avatarBase64: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSubmitting: Boolean = false,
    val isConvertingImage: Boolean = false
) {
    val canSubmit: Boolean
        get() = alias.trim().isNotEmpty() && !isSubmitting && !isLoading && !isConvertingImage
}

/**
 * ViewModel para editar perfil
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Carga el perfil del usuario actual
     * Primero refresca desde el servidor para obtener los datos más actualizados
     */
    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            // Primero refrescar el perfil desde el servidor
            val refreshResult = authRepository.refreshUserProfile()
            if (refreshResult is Resource.Error) {
                android.util.Log.w("EditProfileViewModel", "Error refrescando perfil: ${refreshResult.message}")
                // Continuar con datos locales aunque falle el refresh
            }
            
            // Observar cambios en el usuario actual
            authRepository.getCurrentUser().collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            user = user,
                            alias = user.alias,
                            phone = user.phone ?: "",
                            address = user.address ?: "",
                            avatarUrl = user.avatarUrl ?: "",
                            isLoading = false,
                            error = if (refreshResult is Resource.Error && user.id.isEmpty()) {
                                refreshResult.message
                            } else null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se pudo cargar el perfil"
                        )
                    }
                }
            }
        }
    }

    /**
     * Actualiza el alias
     */
    fun updateAlias(alias: String) {
        _uiState.update { it.copy(alias = alias) }
    }

    /**
     * Actualiza el teléfono
     */
    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    /**
     * Actualiza la dirección
     */
    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    /**
     * Actualiza la URL del avatar
     */
    fun updateAvatarUrl(avatarUrl: String) {
        _uiState.update { it.copy(avatarUrl = avatarUrl) }
    }
    
    /**
     * Selecciona una imagen para el avatar
     */
    fun selectAvatarImage(uri: Uri) {
        _uiState.update { it.copy(selectedAvatarUri = uri) }
    }
    
    /**
     * Convierte la imagen del avatar a base64
     */
    suspend fun convertAvatarToBase64(context: android.content.Context) {
        val uri = _uiState.value.selectedAvatarUri ?: return
        _uiState.update { it.copy(isConvertingImage = true, error = null) }
        
        try {
            android.util.Log.d("EditProfileVM", "Converting avatar URI to base64: $uri")
            
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.util.Log.e("EditProfileVM", "Failed to open input stream for URI: $uri")
                _uiState.update { 
                    it.copy(
                        isConvertingImage = false,
                        error = "Error al abrir la imagen"
                    )
                }
                return
            }
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                android.util.Log.e("EditProfileVM", "Failed to decode bitmap from URI: $uri")
                _uiState.update { 
                    it.copy(
                        isConvertingImage = false,
                        error = "Error al decodificar la imagen"
                    )
                }
                return
            }
            
            android.util.Log.d("EditProfileVM", "Bitmap decoded: ${bitmap.width}x${bitmap.height}")

            // Comprimir imagen (máximo 512x512 para avatares)
            val maxDimension = 512
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
            val base64String = "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            
            android.util.Log.d("EditProfileVM", "Avatar converted to base64 successfully. Size: ${imageBytes.size} bytes")
            
            _uiState.update { 
                it.copy(
                    avatarBase64 = base64String,
                    avatarUrl = base64String, // Actualizar también avatarUrl para que se use al guardar
                    isConvertingImage = false
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("EditProfileVM", "Error converting avatar to base64: ${e.message}", e)
            _uiState.update { 
                it.copy(
                    isConvertingImage = false,
                    error = "Error al procesar la imagen: ${e.message}"
                )
            }
        }
    }

    /**
     * Guarda los cambios del perfil
     */
    fun saveProfile(context: android.content.Context, onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val user = currentState.user ?: return
        if (!currentState.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            // Si hay imagen seleccionada, convertirla primero
            if (currentState.selectedAvatarUri != null && currentState.avatarBase64 == null) {
                convertAvatarToBase64(context)
                if (_uiState.value.error != null) {
                    _uiState.update { it.copy(isSubmitting = false) }
                    return@launch
                }
            }
            
            val finalAvatarUrl = _uiState.value.avatarBase64 ?: currentState.avatarUrl.trim().takeIf { it.isNotEmpty() }

            val updatedUser = user.copy(
                alias = currentState.alias.trim(),
                phone = currentState.phone.trim().takeIf { it.isNotEmpty() },
                address = currentState.address.trim().takeIf { it.isNotEmpty() },
                avatarUrl = finalAvatarUrl
            )

            val result = authRepository.updateProfile(updatedUser)

            _uiState.update { currentState ->
                currentState.copy(
                    isSubmitting = false,
                    error = if (result is Resource.Error) {
                        result.message
                    } else null
                )
            }

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

