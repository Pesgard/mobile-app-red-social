package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuesta de registro
 */
@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "id")
    val id: String, // MongoDB ObjectId viene como String
    
    @Json(name = "first_name")
    val firstName: String,
    
    @Json(name = "last_name")
    val lastName: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "alias")
    val alias: String,
    
    @Json(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @Json(name = "created_at")
    val createdAt: String? = null,
    
    @Json(name = "token")
    val token: String
)



