package com.pesgard.social_network_gera.data.remote.api

import com.pesgard.social_network_gera.data.remote.dto.ChangePasswordRequest
import com.pesgard.social_network_gera.data.remote.dto.CreateCommentRequest
import com.pesgard.social_network_gera.data.remote.dto.CreateCommentResponse
import com.pesgard.social_network_gera.data.remote.dto.CreatePostRequest
import com.pesgard.social_network_gera.data.remote.dto.CreatePostResponse
import com.pesgard.social_network_gera.data.remote.dto.FavoriteRequest
import com.pesgard.social_network_gera.data.remote.dto.LoginRequest
import com.pesgard.social_network_gera.data.remote.dto.LoginResponse
import com.pesgard.social_network_gera.data.remote.dto.MessageResponse
import com.pesgard.social_network_gera.data.remote.dto.PostDto
import com.pesgard.social_network_gera.data.remote.dto.RegisterRequest
import com.pesgard.social_network_gera.data.remote.dto.RegisterResponse
import com.pesgard.social_network_gera.data.remote.dto.SyncRequest
import com.pesgard.social_network_gera.data.remote.dto.SyncResponse
import com.pesgard.social_network_gera.data.remote.dto.UpdatePostRequest
import com.pesgard.social_network_gera.data.remote.dto.UpdateProfileRequest
import com.pesgard.social_network_gera.data.remote.dto.UserDto
import com.pesgard.social_network_gera.data.remote.dto.VoteRequest
import com.pesgard.social_network_gera.data.remote.dto.VoteResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz de Retrofit para todos los endpoints de la API
 */
interface ApiService {
    
    // ============================================================
    // AUTH ENDPOINTS
    // ============================================================
    
    /**
     * Registro de usuario
     * POST /auth/register
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    /**
     * Inicio de sesión
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    /**
     * Obtener perfil del usuario actual
     * GET /users/me
     */
    @GET("users/me")
    suspend fun getCurrentUser(): Response<UserDto>
    
    /**
     * Actualizar perfil del usuario actual
     * PUT /users/me
     */
    @PUT("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<MessageResponse>
    
    /**
     * Cambiar contraseña
     * PUT /users/me/password
     */
    @PUT("users/me/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
    
    // ============================================================
    // POSTS ENDPOINTS
    // ============================================================
    
    /**
     * Listar publicaciones
     * GET /posts
     * 
     * @param search Buscar por título o descripción
     * @param author Filtrar por alias del usuario
     * @param orderBy Ordenar por: title, user, date
     * @param direction Dirección: asc o desc
     */
    @GET("posts")
    suspend fun getPosts(
        @Query("search") search: String? = null,
        @Query("author") author: String? = null,
        @Query("order_by") orderBy: String? = null,
        @Query("direction") direction: String? = null
    ): Response<List<PostDto>>
    
    /**
     * Obtener detalle de publicación
     * GET /posts/{id}
     */
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: String): Response<PostDto>
    
    /**
     * Crear publicación
     * POST /posts
     */
    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<CreatePostResponse>
    
    /**
     * Editar publicación
     * PUT /posts/{id}
     */
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Body request: UpdatePostRequest
    ): Response<MessageResponse>
    
    /**
     * Eliminar publicación
     * DELETE /posts/{id}
     */
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<MessageResponse>
    
    /**
     * Votar en publicación (like/dislike)
     * POST /posts/{id}/vote
     */
    @POST("posts/{id}/vote")
    suspend fun votePost(
        @Path("id") id: String,
        @Body request: VoteRequest
    ): Response<VoteResponse>
    
    /**
     * Agregar/quitar de favoritos
     * POST /posts/{id}/favorite
     */
    @POST("posts/{id}/favorite")
    suspend fun favoritePost(
        @Path("id") id: String,
        @Body request: FavoriteRequest
    ): Response<MessageResponse>
    
    /**
     * Listar favoritos del usuario actual
     * GET /users/me/favorites
     */
    @GET("users/me/favorites")
    suspend fun getFavorites(): Response<List<PostDto>>
    
    /**
     * Obtener posts de un usuario específico
     * GET /posts/user/:userId
     */
    @GET("posts/user/{userId}")
    suspend fun getUserPosts(@Path("userId") userId: String): Response<List<PostDto>>
    
    /**
     * Sincronizar datos pendientes
     * POST /sync
     */
    @POST("sync")
    suspend fun sync(@Body request: SyncRequest): Response<SyncResponse>
    
    // ============================================================
    // COMMENTS ENDPOINTS
    // ============================================================
    
    /**
     * Agregar comentario a un post
     * POST /posts/{postId}/comments
     */
    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): Response<CreateCommentResponse>
    
    /**
     * Responder a un comentario
     * POST /comments/{id}/replies
     */
    @POST("comments/{id}/replies")
    suspend fun replyToComment(
        @Path("id") commentId: String,
        @Body request: CreateCommentRequest
    ): Response<CreateCommentResponse>
    
    /**
     * Like a un comentario
     * POST /comments/{id}/like
     */
    @POST("comments/{id}/like")
    suspend fun likeComment(@Path("id") id: String): Response<MessageResponse>
    
    // ============================================================
    // SYNC ENDPOINTS
    // ============================================================
    
    /**
     * Sincronizar publicaciones offline
     * POST /sync
     */
    @POST("sync")
    suspend fun syncPosts(@Body request: SyncRequest): Response<SyncResponse>
}

/**
 * Request para sincronización de posts offline
 */
data class SyncRequest(
    val pending_posts: List<SyncPost>
)

data class SyncPost(
    val local_id: String,
    val title: String,
    val description: String?,
    val images: List<String>,
    val created_at: String
)

/**
 * Response para sincronización
 */
data class SyncResponse(
    val synced: List<SyncedPost>
)

data class SyncedPost(
    val local_id: String,
    val server_id: Long
)
