package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de cambiar contraseña
 */
@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password")
    val currentPassword: String,
    
    @Json(name = "new_password")
    val newPassword: String
)



