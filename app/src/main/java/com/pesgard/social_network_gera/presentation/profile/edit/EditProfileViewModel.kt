package com.pesgard.social_network_gera.presentation.profile.edit

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
    val isLoading: Boolean = true,
    val error: String? = null,
    val isSubmitting: Boolean = false
) {
    val canSubmit: Boolean
        get() = alias.trim().isNotEmpty() && !isSubmitting && !isLoading
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
     * Guarda los cambios del perfil
     */
    fun saveProfile(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val user = currentState.user ?: return
        if (!currentState.canSubmit) return

        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            val updatedUser = user.copy(
                alias = currentState.alias.trim(),
                phone = currentState.phone.trim().takeIf { it.isNotEmpty() },
                address = currentState.address.trim().takeIf { it.isNotEmpty() },
                avatarUrl = currentState.avatarUrl.trim().takeIf { it.isNotEmpty() }
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

