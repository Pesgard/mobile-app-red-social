package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de actualizar publicación
 */
@JsonClass(generateAdapter = true)
data class UpdatePostRequest(
    @Json(name = "title")
    val title: String? = null,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "images")
    val images: List<String>? = null // base64 encoded images
)



