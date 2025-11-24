package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de crear comentario
 */
@JsonClass(generateAdapter = true)
data class CreateCommentRequest(
    @Json(name = "text")
    val text: String
)



