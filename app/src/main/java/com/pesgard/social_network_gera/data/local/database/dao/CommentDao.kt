package com.pesgard.social_network_gera.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pesgard.social_network_gera.data.local.database.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones de comentarios en Room Database
 */
@Dao
interface CommentDao {
    /**
     * Obtiene todos los comentarios de un post específico
     * Solo obtiene comentarios de nivel superior (sin parent)
     * @param postId ID del post
     * @return Flow que emite la lista de comentarios ordenados por fecha
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE postId = :postId AND parentCommentId IS NULL ORDER BY createdAt ASC")
    fun getCommentsByPostId(postId: Long): Flow<List<CommentEntity>>

    /**
     * Obtiene todas las respuestas (replies) de un comentario específico
     * @param parentCommentId ID del comentario padre
     * @return Flow que emite la lista de respuestas ordenadas por fecha
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE parentCommentId = :parentCommentId ORDER BY createdAt ASC")
    fun getRepliesByCommentId(parentCommentId: Long): Flow<List<CommentEntity>>

    /**
     * Obtiene un comentario por su ID
     * @param id ID del comentario
     * @return Flow que emite el comentario o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE id = :id")
    fun getCommentById(id: Long): Flow<CommentEntity?>

    /**
     * Obtiene un comentario por su serverId
     * @param serverId ID del servidor
     * @return Comentario o null si no existe
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE serverId = :serverId LIMIT 1")
    suspend fun getCommentByServerId(serverId: String): CommentEntity?

    /**
     * Obtiene todos los comentarios no sincronizados
     * @return Flow que emite la lista de comentarios pendientes de sincronización
     */
    @Query("SELECT * FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE synced = 0")
    fun getUnsyncedComments(): Flow<List<CommentEntity>>

    /**
     * Inserta un comentario en la base de datos
     * @param comment Comentario a insertar
     * @return ID del comentario insertado
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    /**
     * Inserta múltiples comentarios
     * @param comments Lista de comentarios a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    /**
     * Actualiza un comentario existente
     * @param comment Comentario a actualizar
     */
    @Update
    suspend fun updateComment(comment: CommentEntity)

    /**
     * Elimina un comentario por su ID
     * @param id ID del comentario a eliminar
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE id = :id")
    suspend fun deleteComment(id: Long)

    /**
     * Elimina todos los comentarios de un post
     * @param postId ID del post
     */
    @Query("DELETE FROM ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: Long)

    /**
     * Marca un comentario como sincronizado
     * @param id ID local del comentario
     * @param serverId ID del servidor asignado
     */
    @Query("UPDATE ${com.pesgard.social_network_gera.util.Constants.Tables.COMMENTS} SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markAsSynced(id: Long, serverId: String)
}
