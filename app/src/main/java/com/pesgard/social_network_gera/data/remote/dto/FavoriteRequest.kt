package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de agregar/quitar favorito
 */
@JsonClass(generateAdapter = true)
data class FavoriteRequest(
    @Json(name = "favorite")
    val favorite: Boolean
)



