package com.pesgard.social_network_gera.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pesgard.social_network_gera.util.Constants

/**
 * Entidad de comentario para Room Database
 * Soporta comentarios anidados (replies) mediante parentCommentId
 */
@Entity(
    tableName = Constants.Tables.COMMENTS,
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CommentEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentCommentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["postId"]),
        Index(value = ["userId"]),
        Index(value = ["parentCommentId"]),
        Index(value = ["synced"]),
        Index(value = ["serverId"])
    ]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val postId: Long,
    val userId: String, // Referencia a UserEntity.id (String)
    
    // Para comentarios anidados (replies)
    val parentCommentId: Long? = null,
    
    val text: String,
    val likes: Int = 0,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Sincronización offline
    val synced: Boolean = true,
    val serverId: String? = null // ID del servidor cuando se sincroniza
)
