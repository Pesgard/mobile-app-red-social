package com.pesgard.social_network_gera.data.local.database.entity

import com.pesgard.social_network_gera.data.local.database.Converters
import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.model.User

/**
 * Extension functions para convertir entre Entities y Domain Models
 */

// ============================================================
// USER CONVERSIONS
// ============================================================

/**
 * Convierte UserEntity a User (Domain Model)
 */
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        alias = alias,
        avatarUrl = avatarUrl,
        phone = phone,
        address = address,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Convierte User (Domain Model) a UserEntity
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email.ifEmpty { "unknown@example.com" }, // Email requerido en UserEntity
        firstName = firstName.ifEmpty { "" }, // Asegurar que no sea null
        lastName = lastName.ifEmpty { "" }, // Asegurar que no sea null
        alias = alias,
        passwordHash = null, // No se guarda la contraseña en el dominio
        phone = phone,
        address = address,
        avatarUrl = avatarUrl,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ============================================================
// POST CONVERSIONS
// ============================================================

/**
 * Convierte PostEntity a Post (Domain Model)
 * @param user Usuario autor del post (opcional, puede venir de una relación)
 */
fun PostEntity.toDomain(user: UserEntity? = null): Post {
    val converters = Converters()
    return Post(
        id = id,
        userId = userId,
        user = user?.toDomain(),
        title = title,
        description = description.orEmpty(),
        images = converters.fromStringList(images),
        likes = likes,
        dislikes = dislikes,
        commentsCount = commentsCount,
        isLiked = false, // Se debe calcular desde FavoriteEntity o VoteEntity
        isDisliked = false, // Se debe calcular desde VoteEntity
        isFavorite = false, // Se debe calcular desde FavoriteEntity
        createdAt = createdAt,
        updatedAt = updatedAt,
        synced = synced,
        serverId = serverId
    )
}

/**
 * Convierte Post (Domain Model) a PostEntity
 */
fun Post.toEntity(): PostEntity {
    val converters = Converters()
    return PostEntity(
        id = id,
        userId = userId,
        title = title,
        description = description,
        images = converters.toStringList(images),
        likes = likes,
        dislikes = dislikes,
        commentsCount = commentsCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        synced = synced,
        serverId = serverId
    )
}

// ============================================================
// COMMENT CONVERSIONS
// ============================================================

/**
 * Convierte CommentEntity a Comment (Domain Model)
 * @param user Usuario que hizo el comentario (opcional)
 * @param replies Lista de respuestas a este comentario (opcional)
 */
fun CommentEntity.toDomain(
    user: UserEntity? = null,
    replies: List<Comment> = emptyList()
): Comment {
    return Comment(
        id = id,
        postId = postId,
        userId = userId,
        user = user?.toDomain(),
        parentCommentId = parentCommentId,
        replies = replies,
        text = text,
        likes = likes,
        isLiked = false, // Se debe calcular desde una relación
        createdAt = createdAt,
        updatedAt = updatedAt,
        synced = synced,
        serverId = serverId
    )
}

/**
 * Convierte Comment (Domain Model) a CommentEntity
 */
fun Comment.toEntity(): CommentEntity {
    return CommentEntity(
        id = id,
        postId = postId,
        userId = userId,
        parentCommentId = parentCommentId,
        text = text,
        likes = likes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        synced = synced,
        serverId = serverId
    )
}



