package com.pesgard.social_network_gera.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO para response de sincronización
 */
@JsonClass(generateAdapter = true)
data class SyncResponse(
    @Json(name = "synced")
    val synced: List<SyncedPostDto> = emptyList()
)

/**
 * DTO para un post sincronizado
 */
@JsonClass(generateAdapter = true)
data class SyncedPostDto(
    @Json(name = "local_id")
    val localId: String,
    
    @Json(name = "server_id")
    val serverId: Long
)

