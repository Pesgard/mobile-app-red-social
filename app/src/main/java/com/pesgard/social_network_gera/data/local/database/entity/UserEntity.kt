package com.pesgard.social_network_gera.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pesgard.social_network_gera.util.Constants

/**
 * Entidad de usuario para Room Database
 */
@Entity(tableName = Constants.Tables.USERS)
data class UserEntity(
    @PrimaryKey
    val id: String = "", // MongoDB ObjectId como String
    
    val email: String,
    val firstName: String,
    val lastName: String,
    val alias: String,
    
    // Password hash (no se guarda la contraseña en texto plano)
    val passwordHash: String? = null,
    
    val phone: String? = null,
    val address: String? = null,
    val avatarUrl: String? = null,
    
    val isActive: Boolean = true,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
