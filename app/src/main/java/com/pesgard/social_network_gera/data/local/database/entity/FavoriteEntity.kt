package com.pesgard.social_network_gera.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pesgard.social_network_gera.util.Constants

/**
 * Entidad de favorito para Room Database
 * Primary key compuesto: userId + postId
 */
@Entity(
    tableName = Constants.Tables.FAVORITES,
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["postId"])
    ],
    primaryKeys = ["userId", "postId"]
)
data class FavoriteEntity(
    val userId: String, // Referencia a UserEntity.id (String)
    val postId: Long,
    val createdAt: Long = System.currentTimeMillis()
)



