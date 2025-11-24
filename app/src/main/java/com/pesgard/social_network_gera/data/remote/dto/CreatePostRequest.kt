package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de crear publicación
 */
@JsonClass(generateAdapter = true)
data class CreatePostRequest(
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "images")
    val images: List<String> = emptyList() // base64 encoded images
)



