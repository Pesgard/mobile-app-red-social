package com.pesgard.social_network_gera.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pesgard.social_network_gera.util.Constants

/**
 * Entidad de publicación para Room Database
 */
@Entity(
    tableName = Constants.Tables.POSTS,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["synced"]),
        Index(value = ["serverId"])
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: String, // Referencia a UserEntity.id (String)
    
    val title: String,
    val description: String? = "",
    
    // Imágenes almacenadas como JSON string o lista separada por comas
    // En Room, podemos usar TypeConverter para convertir List<String> a String
    val images: String = "", // JSON array como string
    
    val likes: Int = 0,
    val dislikes: Int = 0,
    val commentsCount: Int = 0,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Sincronización offline
    val synced: Boolean = true,
    val serverId: String? = null // ID del servidor cuando se sincroniza
)
