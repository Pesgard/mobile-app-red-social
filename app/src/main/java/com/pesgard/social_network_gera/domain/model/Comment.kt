package com.pesgard.social_network_gera.domain.model

/**
 * Modelo de dominio para Comentario
 * Representa un comentario en la capa de dominio
 * Soporta comentarios anidados (replies) mediante parentCommentId
 */
data class Comment(
    val id: Long = 0,
    val postId: Long,
    val userId: String, // Referencia a User.id (String)
    val user: User? = null, // Para mostrar info del usuario que comentó
    val parentCommentId: Long? = null, // Para comentarios anidados (replies)
    val replies: List<Comment> = emptyList(), // Respuestas a este comentario
    val text: String,
    val likes: Int = 0,
    val isLiked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = true,
    val serverId: String? = null
) {
    /**
     * Verifica si el comentario es una respuesta (tiene parent)
     */
    val isReply: Boolean
        get() = parentCommentId != null
    
    /**
     * Verifica si el comentario tiene respuestas
     */
    val hasReplies: Boolean
        get() = replies.isNotEmpty()
}
