package com.pesgard.social_network_gera.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.presentation.auth.WelcomeScreen
import com.pesgard.social_network_gera.presentation.auth.login.LoginScreen
import com.pesgard.social_network_gera.presentation.auth.login.LoginViewModel
import com.pesgard.social_network_gera.presentation.auth.register.RegisterScreen
import com.pesgard.social_network_gera.presentation.auth.register.RegisterViewModel
import com.pesgard.social_network_gera.presentation.feed.FeedScreen
import com.pesgard.social_network_gera.presentation.feed.FeedViewModel

/**
 * NavGraph principal de la aplicación
 * Maneja toda la navegación entre pantallas
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository,
    startDestination: String = Screen.Welcome.route,
    modifier: Modifier = Modifier
) {
    // Observar estado de autenticación
    val isLoggedIn by authRepository.isLoggedIn().collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Feed.route else startDestination,
        modifier = modifier
    ) {
        // ============================================================
        // AUTH SCREENS
        // ============================================================
        composable(Screen.Welcome.route) {
            val loginVM: LoginViewModel = hiltViewModel()
            val registerVM: RegisterViewModel = hiltViewModel()
            
            WelcomeScreen(
                loginViewModel = loginVM,
                registerViewModel = registerVM,
                onAuthSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        // Limpiar el back stack para que no se pueda volver a login
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val loginVM: LoginViewModel = hiltViewModel()
            
            LoginScreen(
                viewModel = loginVM,
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            val registerVM: RegisterViewModel = hiltViewModel()
            
            RegisterScreen(
                viewModel = registerVM,
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // ============================================================
        // MAIN SCREENS
        // ============================================================
        composable(Screen.Feed.route) {
            val feedVM: FeedViewModel = hiltViewModel()
            
            FeedScreen(
                viewModel = feedVM,
                onNavigateToPostDetail = { postId, serverId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId, serverId))
                },
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            val favoritesVM: com.pesgard.social_network_gera.presentation.favorites.FavoritesViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.favorites.FavoritesScreen(
                viewModel = favoritesVM,
                onNavigateToPostDetail = { postId, serverId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId, serverId))
                }
            )
        }

        composable(Screen.Profile.route) {
            val profileVM: com.pesgard.social_network_gera.presentation.profile.ProfileViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.profile.ProfileScreen(
                viewModel = profileVM,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPostDetail = { postId, serverId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId, serverId))
                },
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onNavigateToEditPost = { postId ->
                    navController.navigate(Screen.EditPost.createRoute(postId))
                },
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            // TODO: Implementar SettingsScreen (cambiar contraseña, etc.)
            androidx.compose.material3.Text("Settings Screen - TODO")
        }

        // ============================================================
        // PROFILE SCREENS
        // ============================================================
        composable(Screen.EditProfile.route) {
            val editProfileVM: com.pesgard.social_network_gera.presentation.profile.edit.EditProfileViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.profile.edit.EditProfileScreen(
                viewModel = editProfileVM,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ============================================================
        // POST DETAIL SCREEN
        // ============================================================
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("postId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            val postDetailVM: com.pesgard.social_network_gera.presentation.post.detail.PostDetailViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.post.detail.PostDetailScreen(
                postId = postId,
                serverId = null,
                viewModel = postDetailVM,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.PostDetailWithServerId.route,
            arguments = listOf(
                androidx.navigation.navArgument("postId") {
                    type = androidx.navigation.NavType.LongType
                },
                androidx.navigation.navArgument("serverId") {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            val serverId = backStackEntry.arguments?.getString("serverId")
            val postDetailVM: com.pesgard.social_network_gera.presentation.post.detail.PostDetailViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.post.detail.PostDetailScreen(
                postId = postId,
                serverId = serverId,
                viewModel = postDetailVM,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ============================================================
        // POST SCREENS
        // ============================================================
        composable(Screen.CreatePost.route) {
            val createPostVM: com.pesgard.social_network_gera.presentation.post.create.CreatePostViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.post.create.CreatePostScreen(
                viewModel = createPostVM,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditPost.route,
            arguments = listOf(
                androidx.navigation.navArgument("postId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
            val editPostVM: com.pesgard.social_network_gera.presentation.post.edit.EditPostViewModel = hiltViewModel()
            
            com.pesgard.social_network_gera.presentation.post.edit.EditPostScreen(
                postId = postId,
                viewModel = editPostVM,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ============================================================
        // PROFILE SCREENS
        // ============================================================
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(
                androidx.navigation.navArgument("userId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
            // TODO: Implementar UserProfileScreen
            androidx.compose.material3.Text("User Profile Screen - TODO: $userId")
        }


        // ============================================================
        // SYNC SCREEN
        // ============================================================
        composable(Screen.Sync.route) {
            // TODO: Implementar SyncScreen
            androidx.compose.material3.Text("Sync Screen - TODO")
        }
    }
}
