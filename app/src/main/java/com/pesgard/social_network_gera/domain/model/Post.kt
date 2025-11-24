package com.pesgard.social_network_gera.domain.model

/**
 * Modelo de dominio para Publicación
 * Representa una publicación en la capa de dominio
 */
data class Post(
    val id: Long = 0,
    val userId: String, // Referencia a User.id (String)
    val user: User? = null, // Para mostrar info del autor
    val title: String,
    val description: String = "",
    val images: List<String> = emptyList(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = true,
    val serverId: String? = null
) {
    /**
     * Verifica si el post tiene imágenes
     */
    val hasImages: Boolean
        get() = images.isNotEmpty()
    
    /**
     * Verifica si el post tiene contenido de texto
     */
    val hasTextContent: Boolean
        get() = title.isNotBlank() || description.isNotBlank()
}
