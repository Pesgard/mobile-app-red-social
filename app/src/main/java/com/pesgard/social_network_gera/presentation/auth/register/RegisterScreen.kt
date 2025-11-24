package com.pesgard.social_network_gera.presentation.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.ui.components.AuthTextField
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.PasswordTextField
import com.pesgard.social_network_gera.ui.components.PrimaryButton
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions

/**
 * Pantalla de Registro
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            FullScreenLoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ConnectaSpacing.large)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(ConnectaSpacing.extraLarge))

                // Título
                Text(
                    text = "Crear Cuenta",
                    style = ConnectaTypographyExtensions.appBarTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = ConnectaSpacing.small)
                )

                Text(
                    text = "Completa tus datos para registrarte",
                    style = ConnectaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = ConnectaSpacing.extraLarge)
                )

                // Nombre
                AuthTextField(
                    value = uiState.firstName,
                    onValueChange = viewModel::updateFirstName,
                    label = "Nombre",
                    placeholder = "Juan",
                    isError = !uiState.isFirstNameValid && uiState.firstName.isEmpty(),
                    errorMessage = if (!uiState.isFirstNameValid && uiState.firstName.isEmpty()) {
                        "El nombre es requerido"
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Apellido
                AuthTextField(
                    value = uiState.lastName,
                    onValueChange = viewModel::updateLastName,
                    label = "Apellido",
                    placeholder = "Pérez",
                    isError = !uiState.isLastNameValid && uiState.lastName.isEmpty(),
                    errorMessage = if (!uiState.isLastNameValid && uiState.lastName.isEmpty()) {
                        "El apellido es requerido"
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Email
                AuthTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    label = "Email",
                    placeholder = "tu@email.com",
                    isError = !uiState.isEmailValid,
                    errorMessage = if (!uiState.isEmailValid && uiState.email.isNotEmpty()) {
                        "Email inválido"
                    } else null,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Alias
                AuthTextField(
                    value = uiState.alias,
                    onValueChange = viewModel::updateAlias,
                    label = "Alias",
                    placeholder = "juanperez",
                    isError = !uiState.isAliasValid && uiState.alias.isEmpty(),
                    errorMessage = if (!uiState.isAliasValid && uiState.alias.isEmpty()) {
                        "El alias es requerido"
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Teléfono (opcional)
                AuthTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::updatePhone,
                    label = "Teléfono (opcional)",
                    placeholder = "8123456789",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Dirección (opcional)
                AuthTextField(
                    value = uiState.address,
                    onValueChange = viewModel::updateAddress,
                    label = "Dirección (opcional)",
                    placeholder = "Monterrey, NL",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Contraseña
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Contraseña",
                    placeholder = "Mínimo 10 caracteres",
                    isError = !uiState.isPasswordValid && uiState.password.isNotEmpty(),
                    errorMessage = uiState.passwordValidationMessage,
                    isPasswordVisible = uiState.isPasswordVisible,
                    onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Confirmar contraseña
                PasswordTextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    label = "Confirmar Contraseña",
                    placeholder = "Repite tu contraseña",
                    isError = !uiState.isConfirmPasswordValid && uiState.confirmPassword.isNotEmpty(),
                    errorMessage = if (!uiState.isConfirmPasswordValid && uiState.confirmPassword.isNotEmpty()) {
                        "Las contraseñas no coinciden"
                    } else null,
                    isPasswordVisible = uiState.isConfirmPasswordVisible,
                    onPasswordVisibilityToggle = viewModel::toggleConfirmPasswordVisibility,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.extraLarge))

                // Botón de registro
                PrimaryButton(
                    text = "Registrarse",
                    onClick = { viewModel.register(onRegisterSuccess) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.firstName.isNotEmpty() &&
                             uiState.lastName.isNotEmpty() &&
                             uiState.email.isNotEmpty() &&
                             uiState.isEmailValid &&
                             uiState.password.isNotEmpty() &&
                             uiState.isPasswordValid &&
                             uiState.confirmPassword.isNotEmpty() &&
                             uiState.isConfirmPasswordValid &&
                             uiState.alias.isNotEmpty() &&
                             uiState.isAliasValid
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.medium))

                // Mensaje de error
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        style = ConnectaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = ConnectaSpacing.small)
                    )
                }

                Spacer(modifier = Modifier.height(ConnectaSpacing.large))

                // Link para iniciar sesión
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    style = ConnectaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ConnectaSpacing.small)
                        .clickable { onNavigateToLogin() },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.extraLarge))
            }
        }
    }
}
