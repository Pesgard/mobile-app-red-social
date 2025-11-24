package com.pesgard.social_network_gera.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.presentation.auth.login.LoginScreen
import com.pesgard.social_network_gera.presentation.auth.login.LoginViewModel
import com.pesgard.social_network_gera.presentation.auth.register.RegisterScreen
import com.pesgard.social_network_gera.presentation.auth.register.RegisterViewModel
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography

/**
 * Tipo de pantalla de autenticación
 */
enum class AuthScreenType {
    LOGIN,
    REGISTER
}

/**
 * Pantalla de bienvenida con segmented control para alternar entre Login y Register
 */
@Composable
fun WelcomeScreen(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScreen by remember { mutableStateOf(AuthScreenType.LOGIN) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Segmented Control
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ConnectaSpacing.large)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(ConnectaCustomShapes.input)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón Login
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(ConnectaCustomShapes.input)
                            .background(
                                if (selectedScreen == AuthScreenType.LOGIN) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    androidx.compose.ui.graphics.Color.Transparent
                                }
                            )
                            .clickable { selectedScreen = AuthScreenType.LOGIN }
                            .padding(vertical = ConnectaSpacing.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            style = ConnectaTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedScreen == AuthScreenType.LOGIN) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // Botón Register
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(ConnectaCustomShapes.input)
                            .background(
                                if (selectedScreen == AuthScreenType.REGISTER) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    androidx.compose.ui.graphics.Color.Transparent
                                }
                            )
                            .clickable { selectedScreen = AuthScreenType.REGISTER }
                            .padding(vertical = ConnectaSpacing.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registrarse",
                            style = ConnectaTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedScreen == AuthScreenType.REGISTER) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Contenido según la selección
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedScreen) {
                    AuthScreenType.LOGIN -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = onAuthSuccess,
                            onNavigateToRegister = { selectedScreen = AuthScreenType.REGISTER }
                        )
                    }
                    AuthScreenType.REGISTER -> {
                        RegisterScreen(
                            viewModel = registerViewModel,
                            onRegisterSuccess = onAuthSuccess,
                            onNavigateToLogin = { selectedScreen = AuthScreenType.LOGIN }
                        )
                    }
                }
            }
        }
    }
}


