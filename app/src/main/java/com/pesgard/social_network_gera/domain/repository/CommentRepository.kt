package com.pesgard.social_network_gera.domain.repository

import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de comentarios en la capa de dominio
 * Define las operaciones de comentarios sin depender de implementaciones concretas
 */
interface CommentRepository {
    /**
     * Obtiene todos los comentarios de un post específico
     * @param postId ID del post
     * @return Flow que emite la lista de comentarios (reactivo)
     */
    fun getCommentsByPostId(postId: Long): Flow<List<Comment>>
    
    /**
     * Obtiene un comentario por su ID
     * @param id ID del comentario
     * @return Flow que emite el comentario o null si no existe
     */
    fun getCommentById(id: Long): Flow<Comment?>
    
    /**
     * Crea un nuevo comentario (offline-first: guarda local primero)
     * @param comment Comentario a crear
     * @return Resource con el comentario creado o error
     */
    suspend fun createComment(comment: Comment): Resource<Comment>
    
    /**
     * Responde a un comentario existente (crea un reply)
     * @param parentCommentId ID del comentario padre
     * @param comment Comentario respuesta
     * @return Resource con el comentario creado o error
     */
    suspend fun replyToComment(parentCommentId: Long, comment: Comment): Resource<Comment>
    
    /**
     * Actualiza un comentario existente
     * @param comment Comentario con los datos actualizados
     * @return Resource con el comentario actualizado o error
     */
    suspend fun updateComment(comment: Comment): Resource<Comment>
    
    /**
     * Elimina un comentario
     * @param id ID del comentario a eliminar
     * @return Resource indicando éxito o error
     */
    suspend fun deleteComment(id: Long): Resource<Unit>
    
    /**
     * Da like a un comentario
     * @param commentId ID del comentario
     * @return Resource indicando éxito o error
     */
    suspend fun likeComment(commentId: Long): Resource<Unit>
    
    /**
     * Sincroniza los comentarios pendientes con el servidor
     * @return Resource indicando éxito o error
     */
    suspend fun syncComments(): Resource<Unit>
}
