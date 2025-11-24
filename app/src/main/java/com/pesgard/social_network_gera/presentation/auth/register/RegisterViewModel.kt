package com.pesgard.social_network_gera.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesgard.social_network_gera.domain.model.User
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.util.Resource
import com.pesgard.social_network_gera.util.isValidEmail
import com.pesgard.social_network_gera.util.isValidPassword
import com.pesgard.social_network_gera.util.getPasswordValidationMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la UI para RegisterScreen
 */
data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val address: String = "",
    val alias: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    // Validaciones
    val isFirstNameValid: Boolean = true,
    val isLastNameValid: Boolean = true,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isConfirmPasswordValid: Boolean = true,
    val isAliasValid: Boolean = true,
    val passwordValidationMessage: String? = null
)

/**
 * ViewModel para la pantalla de Registro
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateFirstName(firstName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                firstName = firstName,
                isFirstNameValid = firstName.isNotEmpty(),
                error = null
            )
        }
    }

    fun updateLastName(lastName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                lastName = lastName,
                isLastNameValid = lastName.isNotEmpty(),
                error = null
            )
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                email = email,
                isEmailValid = email.isEmpty() || email.isValidEmail(),
                error = null
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            val isValid = password.isEmpty() || password.isValidPassword()
            val validationMessage = if (password.isNotEmpty() && !isValid) {
                password.getPasswordValidationMessage()
            } else null

            currentState.copy(
                password = password,
                isPasswordValid = isValid,
                passwordValidationMessage = validationMessage,
                isConfirmPasswordValid = currentState.confirmPassword.isEmpty() || 
                    currentState.confirmPassword == password,
                error = null
            )
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = confirmPassword,
                isConfirmPasswordValid = confirmPassword.isEmpty() || 
                    confirmPassword == currentState.password,
                error = null
            )
        }
    }

    fun updatePhone(phone: String) {
        _uiState.update { currentState ->
            currentState.copy(phone = phone, error = null)
        }
    }

    fun updateAddress(address: String) {
        _uiState.update { currentState ->
            currentState.copy(address = address, error = null)
        }
    }

    fun updateAlias(alias: String) {
        _uiState.update { currentState ->
            currentState.copy(
                alias = alias,
                isAliasValid = alias.isNotEmpty(),
                error = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { currentState ->
            currentState.copy(isPasswordVisible = !currentState.isPasswordVisible)
        }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { currentState ->
            currentState.copy(isConfirmPasswordVisible = !currentState.isConfirmPasswordVisible)
        }
    }

    /**
     * Realiza el registro
     */
    fun register(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validaciones
        if (currentState.firstName.isEmpty()) {
            _uiState.update { it.copy(error = "El nombre es requerido") }
            return
        }

        if (currentState.lastName.isEmpty()) {
            _uiState.update { it.copy(error = "El apellido es requerido") }
            return
        }

        if (!currentState.email.isValidEmail()) {
            _uiState.update { it.copy(error = "Por favor ingresa un email válido") }
            return
        }

        if (!currentState.password.isValidPassword()) {
            _uiState.update { 
                it.copy(error = it.passwordValidationMessage ?: "La contraseña no cumple los requisitos") 
            }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden") }
            return
        }

        if (currentState.alias.isEmpty()) {
            _uiState.update { it.copy(error = "El alias es requerido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val user = User(
                email = currentState.email,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                alias = currentState.alias,
                phone = currentState.phone.ifEmpty { null },
                address = currentState.address.ifEmpty { null },
                avatarUrl = currentState.avatarUrl
            )

            val result = authRepository.register(user, currentState.password)

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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
