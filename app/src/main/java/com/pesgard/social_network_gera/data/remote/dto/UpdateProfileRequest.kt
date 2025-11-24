package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de actualizar perfil
 */
@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    @Json(name = "alias")
    val alias: String? = null,
    
    @Json(name = "avatar_url")
    val avatarUrl: String? = null,
    
    @Json(name = "phone")
    val phone: String? = null,
    
    @Json(name = "address")
    val address: String? = null
)



