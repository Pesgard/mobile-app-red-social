package com.pesgard.social_network_gera.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pesgard.social_network_gera.data.local.database.entity.PostEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones de publicaciones en Room Database
 */
@Dao
interface PostDao {
    /**
     * Obtiene todas las publicaciones ordenadas por fecha de creación (más recientes primero)
     * @return Flow que emite la lista de publicaciones
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    /**
     * Obtiene una publicación por su ID
     * @param id ID de la publicación
     * @return Flow que emite la publicación o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE id = :id")
    fun getPostById(id: Long): Flow<PostEntity?>

    /**
     * Obtiene una publicación por su serverId
     * @param serverId ID del servidor
     * @return Flow que emite la publicación o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE serverId = :serverId LIMIT 1")
    suspend fun getPostByServerId(serverId: String): PostEntity?

    /**
     * Obtiene todas las publicaciones de un usuario específico
     * @param userId ID del usuario
     * @return Flow que emite la lista de publicaciones del usuario
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserId(userId: String): Flow<List<PostEntity>>

    /**
     * Obtiene todas las publicaciones no sincronizadas
     * @return Flow que emite la lista de publicaciones pendientes de sincronización
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE synced = 0")
    fun getUnsyncedPosts(): Flow<List<PostEntity>>

    /**
     * Busca publicaciones por título o descripción
     * @param query Texto de búsqueda
     * @return Flow que emite la lista de publicaciones que coinciden con la búsqueda
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPosts(query: String): Flow<List<PostEntity>>

    /**
     * Inserta una publicación en la base de datos
     * @param post Publicación a insertar
     * @return ID de la publicación insertada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    /**
     * Inserta múltiples publicaciones
     * @param posts Lista de publicaciones a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    /**
     * Actualiza una publicación existente
     * @param post Publicación a actualizar
     */
    @Update
    suspend fun updatePost(post: PostEntity)
    
    /**
     * Actualiza el userId de un post por su serverId
     * Útil para corregir posts que tienen userId incorrecto
     */
    @Query("UPDATE ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} SET userId = :userId WHERE serverId = :serverId")
    suspend fun updatePostUserId(serverId: String, userId: String)

    /**
     * Elimina una publicación por su ID
     * @param id ID de la publicación a eliminar
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE id = :id")
    suspend fun deletePost(id: Long)

    /**
     * Elimina una publicación por su serverId
     * @param serverId ID del servidor
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} WHERE serverId = :serverId")
    suspend fun deletePostByServerId(serverId: String)

    /**
     * Marca una publicación como sincronizada
     * @param id ID local de la publicación
     * @param serverId ID del servidor asignado
     */
    @Query("UPDATE ${com.pesgard.social_network_gera.util.Constants.Tables.POSTS} SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markAsSynced(id: Long, serverId: String)
}
