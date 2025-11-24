package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuesta de crear publicación
 */
@JsonClass(generateAdapter = true)
data class CreatePostResponse(
    @Json(name = "id")
    val id: String, // MongoDB ObjectId viene como String
    
    @Json(name = "message")
    val message: String
)



