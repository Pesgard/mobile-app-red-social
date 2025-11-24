package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuestas genéricas con mensaje
 */
@JsonClass(generateAdapter = true)
data class MessageResponse(
    @Json(name = "message")
    val message: String
)



