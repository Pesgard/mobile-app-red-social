package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para respuesta de votar en un post
 */
@JsonClass(generateAdapter = true)
data class VoteResponse(
    @Json(name = "likes")
    val likes: Int,
    
    @Json(name = "dislikes")
    val dislikes: Int
)



