package com.pesgard.social_network_gera.domain.model

/**
 * Modelo de dominio para Borrador de Publicación
 */
data class DraftPost(
    val id: Long = 0,
    val userId: String,
    val title: String,
    val description: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

