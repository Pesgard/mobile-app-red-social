package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para request de sincronización
 */
@JsonClass(generateAdapter = true)
data class SyncRequest(
    @Json(name = "pending_posts")
    val pendingPosts: List<PendingPostDto> = emptyList()
)

/**
 * DTO para un post pendiente de sincronizar
 */
@JsonClass(generateAdapter = true)
data class PendingPostDto(
    @Json(name = "local_id")
    val localId: String,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "description")
    val description: String? = null,
    
    @Json(name = "images")
    val images: List<String> = emptyList(),
    
    @Json(name = "created_at")
    val createdAt: String
)

