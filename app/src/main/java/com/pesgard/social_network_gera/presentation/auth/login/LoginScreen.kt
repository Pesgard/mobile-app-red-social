package com.pesgard.social_network_gera.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * Pantalla de Login
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navegar cuando el login sea exitoso
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.error == null && uiState.email.isNotEmpty()) {
            // El éxito se maneja en el callback onLoginSuccess
        }
    }

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
                verticalArrangement = Arrangement.Center
            ) {
                // Título
                Text(
                    text = "Bienvenido",
                    style = ConnectaTypographyExtensions.appBarTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = ConnectaSpacing.small)
                )

                Text(
                    text = "Inicia sesión para continuar",
                    style = ConnectaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = ConnectaSpacing.extraLarge)
                )

                // Campo de email
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

                // Campo de contraseña
                PasswordTextField(
                    value = uiState.password,
                    onValueChange = viewModel::updatePassword,
                    label = "Contraseña",
                    placeholder = "Ingresa tu contraseña",
                    isError = !uiState.isPasswordValid && uiState.password.isEmpty(),
                    errorMessage = if (!uiState.isPasswordValid && uiState.password.isEmpty()) {
                        "La contraseña es requerida"
                    } else null,
                    isPasswordVisible = uiState.isPasswordVisible,
                    onPasswordVisibilityToggle = viewModel::togglePasswordVisibility,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(ConnectaSpacing.extraLarge))

                // Botón de login
                PrimaryButton(
                    text = "Iniciar Sesión",
                    onClick = { viewModel.login(onLoginSuccess) },
                    isLoading = uiState.isLoading,
                    enabled = uiState.email.isNotEmpty() && 
                             uiState.password.isNotEmpty() && 
                             uiState.isEmailValid
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

                // Link para registrarse
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    style = ConnectaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = ConnectaSpacing.small)
                        .clickable { onNavigateToRegister() },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
