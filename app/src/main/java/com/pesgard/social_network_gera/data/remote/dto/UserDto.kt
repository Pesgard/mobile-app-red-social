package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuestas de usuario de la API
 */
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id")
    val id: String, // MongoDB ObjectId viene como String
    
    @Json(name = "first_name")
    val firstName: String? = null, // Opcional porque el author en posts solo tiene alias
    
    @Json(name = "last_name")
    val lastName: String? = null, // Opcional porque el author en posts solo tiene alias
    
    @Json(name = "email")
    val email: String? = null,
    
    @Json(name = "alias")
    val alias: String,
    
    @Json(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @Json(name = "phone")
    val phone: String? = null,
    
    @Json(name = "address")
    val address: String? = null,
    
    @Json(name = "created_at")
    val createdAt: String? = null,
    
    @Json(name = "updated_at")
    val updatedAt: String? = null
)
