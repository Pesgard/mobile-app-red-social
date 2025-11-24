package com.pesgard.social_network_gera.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.util.Resource
import com.pesgard.social_network_gera.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para LoginScreen
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true
)

/**
 * ViewModel para la pantalla de Login
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el email y valida en tiempo real
     */
    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                isEmailValid = email.isEmpty() || email.isValidEmail(),
                error = null // Limpiar error al escribir
            )
        }
    }

    /**
     * Actualiza la contraseña
     */
    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = password,
                isPasswordValid = password.isNotEmpty(),
                error = null // Limpiar error al escribir
            )
        }
    }

    /**
     * Alterna la visibilidad de la contraseña
     */
    fun togglePasswordVisibility() {
        _uiState.update { currentState ->
            currentState.copy(isPasswordVisible = !currentState.isPasswordVisible)
        }
    }

    /**
     * Realiza el login
     */
    fun login(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validaciones
        if (!currentState.email.isValidEmail()) {
            _uiState.update { it.copy(error = "Por favor ingresa un email válido") }
            return
        }

        if (currentState.password.isEmpty()) {
            _uiState.update { it.copy(error = "Por favor ingresa tu contraseña") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.login(currentState.email, currentState.password)

            _uiState.update { state ->
                when (result) {
                    is Resource.Success -> {
                        onSuccess()
                        state.copy(isLoading = false, error = null)
                    }
                    is Resource.Error -> {
                        state.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {
                        state.copy(isLoading = true)
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
