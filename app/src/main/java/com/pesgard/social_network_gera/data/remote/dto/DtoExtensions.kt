package com.pesgard.social_network_gera.data.remote.dto

import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.model.User
import com.pesgard.social_network_gera.util.toTimestamp

/**
 * Extension functions para convertir entre DTOs y Domain Models
 */

// ============================================================
// USER CONVERSIONS
// ============================================================

/**
 * Convierte UserDto a User (Domain Model)
 */
fun UserDto.toDomain(): User {
    return User(
        id = id, // Ya es String
        email = email.orEmpty(),
        firstName = firstName.orEmpty(),
        lastName = lastName.orEmpty(),
        alias = alias,
        avatarUrl = avatarUrl,
        phone = phone,
        address = address,
        createdAt = createdAt?.toTimestamp() ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.toTimestamp() ?: System.currentTimeMillis()
    )
}

// ============================================================
// POST CONVERSIONS
// ============================================================

/**
 * Convierte PostDto a Post (Domain Model)
 */
fun PostDto.toDomain(): Post {
    return Post(
        id = 0, // ID local se asignará al insertar en Room
        userId = author?.id ?: "", // author.id es String (ObjectId)
        user = author?.toDomain(),
        title = title,
        description = description.orEmpty(),
        images = images,
        likes = likes,
        dislikes = dislikes,
        commentsCount = metadata?.totalComments ?: comments?.size ?: 0,
        createdAt = createdAt?.toTimestamp() ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.toTimestamp() ?: System.currentTimeMillis(),
        synced = true,
        serverId = id, // id ya es String (MongoDB ObjectId)
        // Usar user_interaction del backend si está disponible
        isLiked = userInteraction?.hasLiked ?: false,
        isDisliked = userInteraction?.hasDisliked ?: false,
        isFavorite = userInteraction?.isFavorite ?: false
    )
}

// ============================================================
// COMMENT CONVERSIONS
// ============================================================

/**
 * Convierte CommentDto a Comment (Domain Model)
 * Convierte recursivamente las replies
 * @param postId ID del post (requerido, debe establecerse desde el contexto)
 * @param parentCommentId ID del comentario padre si es una respuesta (opcional)
 */
fun CommentDto.toDomain(
    postId: Long = 0, // Se debe establecer desde el contexto
    parentCommentId: Long? = null // Se establece desde el contexto si es reply
): Comment {
    // Nota: Las replies se procesan recursivamente, pero el parentCommentId se establecerá
    // cuando se guarde en la base de datos local usando el serverId del comentario padre
    return Comment(
        id = 0, // ID local se asignará al insertar en Room
        postId = postId,
        userId = user?.id ?: "", // user.id es String (ObjectId)
        user = user?.toDomain(),
        parentCommentId = parentCommentId,
        replies = replies?.map { 
            // Las replies se procesan recursivamente, pero el parentCommentId se establecerá
            // cuando se guarde en la base de datos usando el serverId del comentario padre
            it.toDomain(postId = postId, parentCommentId = null)
        } ?: emptyList(),
        text = text,
        likes = likes,
        createdAt = createdAt?.toTimestamp() ?: System.currentTimeMillis(),
        updatedAt = updatedAt?.toTimestamp() ?: System.currentTimeMillis(),
        synced = true,
        serverId = id, // id ya es String (MongoDB ObjectId)
        // Usar user_interaction del backend si está disponible
        isLiked = userInteraction?.hasLiked ?: false
    )
}

