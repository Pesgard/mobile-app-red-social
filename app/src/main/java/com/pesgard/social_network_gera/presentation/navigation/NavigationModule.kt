package com.pesgard.social_network_gera.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.pesgard.social_network_gera.domain.repository.AuthRepository

/**
 * Módulo de navegación que proporciona el NavController y configura las rutas
 */
@Composable
fun SetupNavigation(
    authRepository: AuthRepository,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    AppNavGraph(
        navController = navController,
        authRepository = authRepository,
        modifier = modifier
    )
}

