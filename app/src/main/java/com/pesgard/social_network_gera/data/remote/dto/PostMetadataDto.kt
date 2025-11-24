package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para metadata de un post
 */
@JsonClass(generateAdapter = true)
data class PostMetadataDto(
    @Json(name = "comments_count")
    val commentsCount: Int = 0,
    
    @Json(name = "total_comments")
    val totalComments: Int = 0,
    
    @Json(name = "has_images")
    val hasImages: Boolean = false,
    
    @Json(name = "image_count")
    val imageCount: Int = 0
)


