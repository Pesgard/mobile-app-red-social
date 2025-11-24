package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de votar (like/dislike) en un post
 */
@JsonClass(generateAdapter = true)
data class VoteRequest(
    @Json(name = "vote")
    val vote: String // "like" o "dislike"
)



