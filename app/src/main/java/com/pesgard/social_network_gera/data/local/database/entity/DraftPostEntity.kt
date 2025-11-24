package com.pesgard.social_network_gera.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pesgard.social_network_gera.util.Constants

/**
 * Entidad de borrador de publicación para Room Database
 */
@Entity(
    tableName = Constants.Tables.DRAFTS,
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
        Index(value = ["updatedAt"])
    ]
)
data class DraftPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: String, // Referencia a UserEntity.id (String)
    
    val title: String,
    val description: String? = "",
    
    // Imágenes almacenadas como JSON string
    val images: String = "", // JSON array como string
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

