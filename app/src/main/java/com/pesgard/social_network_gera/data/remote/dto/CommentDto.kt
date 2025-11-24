package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuestas de comentarios de la API
 */
@JsonClass(generateAdapter = true)
data class CommentDto(
    @Json(name = "id")
    val id: String, // MongoDB ObjectId viene como String
    
    @Json(name = "user")
    val user: UserDto? = null,
    
    @Json(name = "text")
    val text: String,
    
    @Json(name = "likes")
    val likes: Int = 0,
    
    @Json(name = "replies")
    val replies: List<CommentDto>? = null,
    
    @Json(name = "created_at")
    val createdAt: String? = null,
    
    @Json(name = "updated_at")
    val updatedAt: String? = null,
    
    @Json(name = "user_interaction")
    val userInteraction: UserInteractionDto? = null
)



