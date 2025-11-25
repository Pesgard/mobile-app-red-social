package com.pesgard.social_network_gera.data.repository

import com.pesgard.social_network_gera.data.local.database.dao.CommentDao
import com.pesgard.social_network_gera.data.local.database.dao.PostDao
import com.pesgard.social_network_gera.data.local.database.dao.UserDao
import com.pesgard.social_network_gera.data.local.database.entity.UserEntity
import com.pesgard.social_network_gera.data.local.database.entity.toDomain
import com.pesgard.social_network_gera.data.local.database.entity.toEntity
import com.pesgard.social_network_gera.data.remote.api.ApiService
import com.pesgard.social_network_gera.data.remote.dto.CreateCommentRequest
import com.pesgard.social_network_gera.data.remote.dto.toDomain
import com.pesgard.social_network_gera.domain.model.Comment
import com.pesgard.social_network_gera.domain.repository.CommentRepository
import com.pesgard.social_network_gera.util.Constants.ErrorMessages
import com.pesgard.social_network_gera.util.NetworkMonitor
import com.pesgard.social_network_gera.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del repositorio de comentarios
 * Maneja la lógica offline-first: guarda localmente primero, luego sincroniza
 */
class CommentRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val commentDao: CommentDao,
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val networkMonitor: NetworkMonitor
) : CommentRepository {

    override fun getCommentsByPostId(postId: Long): Flow<List<Comment>> {
        android.util.Log.d("CommentRepository", "getCommentsByPostId llamado con postId: $postId")
        return commentDao.getCommentsByPostId(postId).map { entities ->
            android.util.Log.d("CommentRepository", "Comentarios encontrados en DB: ${entities.size}")
            entities.forEachIndexed { index, entity ->
                android.util.Log.d("CommentRepository", "  [$index] CommentId: ${entity.id}, postId: ${entity.postId}, userId: ${entity.userId}, texto: ${entity.text.take(30)}")
            }
            // Convertir comentarios de nivel superior y cargar replies
            entities.map { entity ->
                // Obtener UserEntity (no convertirlo a User todavía)
                val userEntity = userDao.getUserById(entity.userId).firstOrNull()
                val replies = commentDao.getRepliesByCommentId(entity.id).firstOrNull()?.map { replyEntity ->
                    // Obtener UserEntity para la respuesta
                    val replyUserEntity = userDao.getUserById(replyEntity.userId).firstOrNull()
                    replyEntity.toDomain(user = replyUserEntity, replies = emptyList())
                } ?: emptyList()
                
                entity.toDomain(user = userEntity, replies = replies)
            }
        }
    }

    override fun getCommentById(id: Long): Flow<Comment?> {
        return commentDao.getCommentById(id).map { entity ->
            entity?.let {
                // Obtener UserEntity (no convertirlo a User todavía)
                val userEntity = userDao.getUserById(it.userId).firstOrNull()
                val replies = if (it.parentCommentId == null) {
                    commentDao.getRepliesByCommentId(it.id).firstOrNull()?.map { replyEntity ->
                        // Obtener UserEntity para la respuesta
                        val replyUserEntity = userDao.getUserById(replyEntity.userId).firstOrNull()
                        replyEntity.toDomain(user = replyUserEntity, replies = emptyList())
                    } ?: emptyList()
                } else {
                    emptyList()
                }
                it.toDomain(user = userEntity, replies = replies)
            }
        }
    }

    override suspend fun createComment(comment: Comment): Resource<Comment> {
        // 1. Guardar localmente primero (offline-first)
        val localComment = comment.copy(
            synced = false,
            serverId = null
        )
        val localId = commentDao.insertComment(localComment.toEntity())

        // 2. Intentar sincronizar con servidor si hay conexión
        if (networkMonitor.isOnline()) {
            return try {
                // Obtener el serverId del post
                val post = postDao.getPostById(comment.postId).firstOrNull()
                val postServerId = post?.serverId
                
                if (postServerId.isNullOrEmpty()) {
                    // El post no está sincronizado, guardar localmente
                    return Resource.Success(localComment.copy(id = localId))
                }
                
                val request = CreateCommentRequest(text = comment.text)
                val response = apiService.createComment(postServerId, request)

                if (response.isSuccessful && response.body() != null) {
                    val createResponse = response.body()!!
                    val syncedComment = comment.copy(
                        id = localId,
                        synced = true,
                        serverId = createResponse.id // id ya es String
                    )

                    // Actualizar en DB local con serverId
                    commentDao.updateComment(syncedComment.toEntity())
                    commentDao.markAsSynced(localId, createResponse.id)

                    Resource.Success(syncedComment)
                } else {
                    // Error en servidor, pero ya está guardado localmente
                    Resource.Success(localComment.copy(id = localId))
                }
            } catch (e: Exception) {
                // Error de red, pero ya está guardado localmente
                Resource.Success(localComment.copy(id = localId))
            }
        } else {
            // Sin conexión, solo guardado local
            return Resource.Success(localComment.copy(id = localId))
        }
    }

    override suspend fun replyToComment(
        parentCommentId: Long,
        comment: Comment
    ): Resource<Comment> {
        // 1. Guardar localmente primero (offline-first)
        val localComment = comment.copy(
            parentCommentId = parentCommentId,
            synced = false,
            serverId = null
        )
        val localId = commentDao.insertComment(localComment.toEntity())

        // 2. Intentar sincronizar con servidor si hay conexión
        if (networkMonitor.isOnline()) {
            return try {
                // Obtener el serverId del comentario padre
                val parentComment = commentDao.getCommentById(parentCommentId).firstOrNull()
                val parentServerId = parentComment?.serverId

                if (!parentServerId.isNullOrEmpty()) {
                    val request = CreateCommentRequest(text = comment.text)
                    val response = apiService.replyToComment(parentServerId, request)

                    if (response.isSuccessful && response.body() != null) {
                        val createResponse = response.body()!!
                        val syncedComment = comment.copy(
                            id = localId,
                            parentCommentId = parentCommentId,
                            synced = true,
                            serverId = createResponse.id // id ya es String
                        )

                        // Actualizar en DB local
                        commentDao.updateComment(syncedComment.toEntity())
                        commentDao.markAsSynced(localId, createResponse.id)

                        Resource.Success(syncedComment)
                    } else {
                        Resource.Success(localComment.copy(id = localId))
                    }
                } else {
                    // El comentario padre no está sincronizado, guardar localmente
                    Resource.Success(localComment.copy(id = localId))
                }
            } catch (e: Exception) {
                Resource.Success(localComment.copy(id = localId))
            }
        } else {
            return Resource.Success(localComment.copy(id = localId))
        }
    }

    override suspend fun updateComment(comment: Comment): Resource<Comment> {
        // 1. Actualizar localmente primero
        val updatedComment = comment.copy(updatedAt = System.currentTimeMillis())
        commentDao.updateComment(updatedComment.toEntity())

        // 2. Intentar sincronizar con servidor si hay conexión y tiene serverId
        if (networkMonitor.isOnline() && comment.serverId != null) {
            return try {
                // TODO: Implementar endpoint de actualización de comentario si existe
                // Por ahora, solo actualizamos localmente
                Resource.Success(updatedComment.copy(synced = false))
            } catch (e: Exception) {
                Resource.Success(updatedComment.copy(synced = false))
            }
        } else {
            return Resource.Success(updatedComment.copy(synced = false))
        }
    }

    override suspend fun deleteComment(id: Long): Resource<Unit> {
        // 1. Eliminar localmente
        val comment = commentDao.getCommentById(id).firstOrNull()
        commentDao.deleteComment(id)

        // 2. Intentar eliminar en servidor si hay conexión y tiene serverId
        if (networkMonitor.isOnline() && comment?.serverId != null) {
            return try {
                // TODO: Implementar endpoint de eliminación de comentario si existe
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Success(Unit) // Ya eliminado localmente
            }
        } else {
            return Resource.Success(Unit)
        }
    }

    override suspend fun likeComment(commentId: Long): Resource<Unit> {
        if (networkMonitor.isOnline()) {
            return try {
                // Obtener el serverId del comentario
                val comment = commentDao.getCommentById(commentId).firstOrNull()
                val serverId = comment?.serverId

                if (!serverId.isNullOrEmpty()) {
                    val response = apiService.likeComment(serverId)
                    if (response.isSuccessful) {
                        // Actualizar likes en DB local (incrementar)
                        comment?.let {
                            val updated = it.copy(likes = it.likes + 1)
                            commentDao.updateComment(updated)
                        }
                        Resource.Success(Unit)
                    } else {
                        Resource.Error(ErrorMessages.NETWORK_ERROR)
                    }
                } else {
                    Resource.Error("Comentario no sincronizado")
                }
            } catch (e: Exception) {
                Resource.Error(ErrorMessages.NETWORK_ERROR, e)
            }
        } else {
            return Resource.Error(ErrorMessages.OFFLINE_ERROR)
        }
    }

    override suspend fun syncComments(): Resource<Unit> {
        if (!networkMonitor.isOnline()) {
            return Resource.Error(ErrorMessages.OFFLINE_ERROR)
        }

        return try {
            // Obtener comentarios no sincronizados
            val unsyncedComments = commentDao.getUnsyncedComments().firstOrNull() ?: emptyList()

            if (unsyncedComments.isEmpty()) {
                return Resource.Success(Unit)
            }

            // Intentar sincronizar cada comentario
            unsyncedComments.forEach { entity ->
                val comment = entity.toDomain()
                if (comment.serverId == null) {
                    if (comment.parentCommentId != null) {
                        // Es una respuesta
                        replyToComment(comment.parentCommentId!!, comment)
                    } else {
                        // Es un comentario de nivel superior
                        createComment(comment)
                    }
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(ErrorMessages.NETWORK_ERROR, e)
        }
    }
}
