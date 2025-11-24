package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuesta de login
 */
@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "token")
    val token: String,
    
    @Json(name = "user")
    val user: UserDto
)



