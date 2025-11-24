package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuestas de publicaciones de la API
 */
@JsonClass(generateAdapter = true)
data class PostDto(
    @Json(name = "id")
    val id: String, // MongoDB ObjectId viene como String
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "author")
    val author: UserDto? = null,
    
    @Json(name = "images")
    val images: List<String> = emptyList(),
    
    @Json(name = "likes")
    val likes: Int = 0,
    
    @Json(name = "dislikes")
    val dislikes: Int = 0,
    
    @Json(name = "comments")
    val comments: List<CommentDto>? = null,
    
    @Json(name = "created_at")
    val createdAt: String? = null,
    
    @Json(name = "updated_at")
    val updatedAt: String? = null,
    
    @Json(name = "user_interaction")
    val userInteraction: UserInteractionDto? = null,
    
    @Json(name = "metadata")
    val metadata: PostMetadataDto? = null
)
