package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para información de interacción del usuario con un post o comentario
 */
@JsonClass(generateAdapter = true)
data class UserInteractionDto(
    @Json(name = "has_liked")
    val hasLiked: Boolean = false,
    
    @Json(name = "has_disliked")
    val hasDisliked: Boolean = false,
    
    @Json(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @Json(name = "can_edit")
    val canEdit: Boolean = false,
    
    @Json(name = "can_delete")
    val canDelete: Boolean = false
)



