package com.pesgard.social_network_gera.domain.repository

import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de publicaciones en la capa de dominio
 * Define las operaciones de publicaciones sin depender de implementaciones concretas
 */
interface PostRepository {
    /**
     * Obtiene todas las publicaciones
     * @return Flow que emite la lista de publicaciones (reactivo, se actualiza automáticamente)
     */
    fun getPosts(): Flow<List<Post>>
    
    /**
     * Obtiene una publicación por su ID
     * @param id ID de la publicación
     * @return Flow que emite la publicación o null si no existe
     */
    fun getPostById(id: Long): Flow<Post?>
    
    /**
     * Obtiene una publicación por su serverId
     * @param serverId ID del servidor
     * @return Flow que emite la publicación o null si no existe
     */
    fun getPostByServerId(serverId: String): Flow<Post?>
    
    /**
     * Obtiene todas las publicaciones de un usuario específico
     * @param userId ID del usuario
     * @return Flow que emite la lista de publicaciones del usuario
     */
    fun getPostsByUserId(userId: String): Flow<List<Post>>
    
    /**
     * Crea una nueva publicación (offline-first: guarda local primero)
     * @param post Publicación a crear
     * @return Resource con la publicación creada o error
     */
    suspend fun createPost(post: Post): Resource<Post>
    
    /**
     * Actualiza una publicación existente
     * @param post Publicación con los datos actualizados
     * @return Resource con la publicación actualizada o error
     */
    suspend fun updatePost(post: Post): Resource<Post>
    
    /**
     * Elimina una publicación
     * @param id ID de la publicación a eliminar
     * @return Resource indicando éxito o error
     */
    suspend fun deletePost(id: Long): Resource<Unit>
    
    /**
     * Da like a una publicación
     * @param postId ID de la publicación
     * @return Resource indicando éxito o error
     */
    suspend fun likePost(postId: Long): Resource<Unit>
    
    /**
     * Da dislike a una publicación
     * @param postId ID de la publicación
     * @return Resource indicando éxito o error
     */
    suspend fun dislikePost(postId: Long): Resource<Unit>
    
    /**
     * Agrega una publicación a favoritos
     * @param postId ID de la publicación
     * @return Resource indicando éxito o error
     */
    suspend fun favoritePost(postId: Long): Resource<Unit>
    
    /**
     * Quita una publicación de favoritos
     * @param postId ID de la publicación
     * @return Resource indicando éxito o error
     */
    suspend fun unfavoritePost(postId: Long): Resource<Unit>
    
    /**
     * Busca publicaciones por título o descripción
     * @param query Texto de búsqueda
     * @return Flow que emite la lista de publicaciones que coinciden
     */
    fun searchPosts(query: String): Flow<List<Post>>
    
    /**
     * Obtiene las publicaciones favoritas del usuario actual
     * @return Flow que emite la lista de publicaciones favoritas
     */
    fun getFavorites(): Flow<List<Post>>
    
    /**
     * Sincroniza las publicaciones pendientes con el servidor
     * @return Resource indicando éxito o error
     */
    suspend fun syncPosts(): Resource<Unit>
    
    /**
     * Refresca las publicaciones desde el servidor (si hay conexión)
     * @return Resource indicando éxito o error
     */
    suspend fun refreshPosts(): Resource<Unit>
    
    /**
     * Refresca un post específico desde el servidor y guarda sus comentarios
     * @param serverId ID del servidor del post
     * @return Resource indicando éxito o error
     */
    suspend fun refreshPostById(serverId: String): Resource<Unit>
}
