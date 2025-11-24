package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de registro de usuario
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "first_name")
    val firstName: String,
    
    @Json(name = "last_name")
    val lastName: String,
    
    @Json(name = "email")
    val email: String,
    
    @Json(name = "password")
    val password: String,
    
    @Json(name = "phone")
    val phone: String? = null,
    
    @Json(name = "address")
    val address: String? = null,
    
    @Json(name = "alias")
    val alias: String,
    
    @Json(name = "avatar_url")
    val avatarUrl: String? = null
)



