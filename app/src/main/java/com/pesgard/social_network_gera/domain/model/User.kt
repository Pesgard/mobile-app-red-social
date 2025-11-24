package com.pesgard.social_network_gera.domain.model

/**
 * Modelo de dominio para Usuario
 * Representa un usuario en la capa de dominio (sin dependencias de frameworks)
 */
data class User(
    val id: String = "", // MongoDB ObjectId como String
    val email: String,
    val firstName: String,
    val lastName: String,
    val alias: String,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Nombre completo del usuario
     */
    val fullName: String
        get() = "$firstName $lastName"
}
