package com.pesgard.social_network_gera.presentation.navigation

/**
 * Sealed class que define todas las rutas de navegación de la aplicación
 */
sealed class Screen(val route: String) {
    // ============================================================
    // AUTH SCREENS
    // ============================================================
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")

    // ============================================================
    // MAIN SCREENS
    // ============================================================
    object Feed : Screen("feed")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object Settings : Screen("settings")

    // ============================================================
    // POST SCREENS
    // ============================================================
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: Long, serverId: String? = null) = 
            if (serverId != null) {
                "post_detail/$postId/server/$serverId"
            } else {
                "post_detail/$postId"
            }
    }
    object PostDetailWithServerId : Screen("post_detail/{postId}/server/{serverId}")

    object CreatePost : Screen("create_post")
    object EditPost : Screen("edit_post/{postId}") {
        fun createRoute(postId: Long) = "edit_post/$postId"
    }

    // ============================================================
    // PROFILE SCREENS
    // ============================================================
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: Long) = "user_profile/$userId"
    }

    object EditProfile : Screen("edit_profile")

    // ============================================================
    // SYNC SCREEN
    // ============================================================
    object Sync : Screen("sync")
}


