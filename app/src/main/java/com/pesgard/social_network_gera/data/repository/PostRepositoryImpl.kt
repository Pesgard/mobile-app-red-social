package com.pesgard.social_network_gera.data.repository

import com.pesgard.social_network_gera.data.local.AppDatabase
import com.pesgard.social_network_gera.data.local.database.dao.CommentDao
import com.pesgard.social_network_gera.data.local.database.dao.FavoriteDao
import com.pesgard.social_network_gera.data.local.database.dao.PostDao
import com.pesgard.social_network_gera.data.local.database.dao.UserDao
import com.pesgard.social_network_gera.data.local.database.entity.CommentEntity
import com.pesgard.social_network_gera.data.local.database.entity.FavoriteEntity
import com.pesgard.social_network_gera.data.local.database.entity.PostEntity
import com.pesgard.social_network_gera.data.local.database.entity.toDomain
import com.pesgard.social_network_gera.data.local.database.entity.toEntity
import com.pesgard.social_network_gera.data.local.datastore.SessionManager
import com.pesgard.social_network_gera.data.remote.api.ApiService
import com.pesgard.social_network_gera.data.remote.dto.CreatePostRequest
import com.pesgard.social_network_gera.data.remote.dto.FavoriteRequest
import com.pesgard.social_network_gera.data.remote.dto.PostDto
import com.pesgard.social_network_gera.data.remote.dto.UpdatePostRequest
import com.pesgard.social_network_gera.data.remote.dto.VoteRequest
import com.pesgard.social_network_gera.data.remote.dto.toDomain
import com.pesgard.social_network_gera.domain.model.Post
import com.pesgard.social_network_gera.domain.repository.PostRepository
import com.pesgard.social_network_gera.util.Constants
import com.pesgard.social_network_gera.util.NetworkMonitor
import com.pesgard.social_network_gera.util.Resource
import androidx.room.withTransaction
import com.pesgard.social_network_gera.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementación del repositorio de publicaciones
 * Maneja la lógica offline-first: guarda localmente primero, luego sincroniza
 */
class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase,
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val commentDao: CommentDao,
    private val favoriteDao: FavoriteDao,
    private val sessionManager: SessionManager,
    private val networkMonitor: NetworkMonitor
) : PostRepository {

    override fun getPosts(): Flow<List<Post>> {
        // Combinar posts con información de favoritos y usuarios de forma reactiva
        return sessionManager.userId.flatMapLatest { userId ->
            val favoritesFlow = if (userId != null) {
                favoriteDao.getFavoritesByUserId(userId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList<com.pesgard.social_network_gera.data.local.database.entity.FavoriteEntity>())
            }
            
            postDao.getAllPosts().flatMapLatest { posts ->
                // Obtener IDs únicos de usuarios
                val userIds = posts.map { it.userId }.distinct()
                
                // Cargar usuarios de forma reactiva
                val usersFlow = if (userIds.isNotEmpty()) {
                    userDao.getUsersByIds(userIds)
                } else {
                    kotlinx.coroutines.flow.flowOf(emptyList())
                }
                
                combine(
                    kotlinx.coroutines.flow.flowOf(posts),
                    favoritesFlow,
                    usersFlow
                ) { postsList, favorites, users ->
                    val userMap = users.associateBy { it.id }
                    val favoritePostIds = favorites.map { it.postId }.toSet()
                    
                    postsList.map { entity ->
                        val userEntity = userMap[entity.userId]
                        entity.toDomain(user = userEntity).copy(
                            isFavorite = favoritePostIds.contains(entity.id)
                        )
                    }
                }
            }
        }
    }

    override fun getPostById(id: Long): Flow<Post?> {
        return sessionManager.userId.flatMapLatest { userId ->
            val isFavoriteFlow = if (userId != null) {
                favoriteDao.isFavorite(userId, id)
            } else {
                kotlinx.coroutines.flow.flowOf(false)
            }
            
            postDao.getPostById(id).flatMapLatest { postEntity ->
                if (postEntity == null) {
                    kotlinx.coroutines.flow.flowOf(null)
                } else {
                    // Cargar usuario del post
                    val userFlow = userDao.getUserById(postEntity.userId)
                    
                    combine(
                        kotlinx.coroutines.flow.flowOf(postEntity),
                        isFavoriteFlow,
                        userFlow
                    ) { entity, isFav, userEntity ->
                        (entity as? PostEntity)?.toDomain(
                            user = userEntity as? com.pesgard.social_network_gera.data.local.database.entity.UserEntity
                        )?.copy(isFavorite = isFav ?: false)
                    }
                }
            }
        }
    }

    override fun getPostByServerId(serverId: String): Flow<Post?> {
        return kotlinx.coroutines.flow.flow {
            val entity = postDao.getPostByServerId(serverId)
            if (entity != null) {
                val userId = getCurrentUserId()
                val isFav = favoriteDao.isFavorite(userId, entity.id).firstOrNull() ?: false
                emit(entity.toDomain().copy(isFavorite = isFav))
            } else {
                emit(null)
            }
        }
    }

    override fun getPostsByUserId(userId: String): Flow<List<Post>> {
        return sessionManager.userId.flatMapLatest { currentUserId ->
            val favoritesFlow = if (currentUserId != null) {
                favoriteDao.getFavoritesByUserId(currentUserId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList<com.pesgard.social_network_gera.data.local.database.entity.FavoriteEntity>())
            }
            
            combine(
                postDao.getPostsByUserId(userId),
                favoritesFlow
            ) { posts, favorites ->
                val favoritePostIds = favorites.map { it.postId }.toSet()
                posts.map { entity ->
                    entity.toDomain().copy(
                        isFavorite = favoritePostIds.contains(entity.id)
                    )
                }
            }
        }
    }

    override suspend fun createPost(post: Post): Resource<Post> {
        // 1. Guardar localmente primero (offline-first)
        val localPost = post.copy(
            synced = false,
            serverId = null
        )
        val localId = postDao.insertPost(localPost.toEntity())

        // 2. Intentar sincronizar con servidor si hay conexión
        if (networkMonitor.isOnline()) {
            return try {
                val request = CreatePostRequest(
                    title = post.title,
                    description = post.description,
                    images = post.images // Imágenes ya en base64 desde CreatePostViewModel
                )

                val response = apiService.createPost(request)

                if (response.isSuccessful && response.body() != null) {
                    val createResponse = response.body()!!
                    val serverPost = post.copy(
                        id = localId, // Mantener ID local
                        synced = true,
                        serverId = createResponse.id // id ya es String
                    )

                    // Actualizar en DB local con serverId
                    postDao.updatePost(serverPost.toEntity())
                    postDao.markAsSynced(localId, createResponse.id)

                    Resource.Success(serverPost)
                } else {
                    // Error en servidor, pero ya está guardado localmente
                    Resource.Success(localPost.copy(id = localId))
                }
            } catch (e: Exception) {
                // Error de red, pero ya está guardado localmente
                Resource.Success(localPost.copy(id = localId))
            }
        } else {
            // Sin conexión, solo guardado local
            return Resource.Success(localPost.copy(id = localId))
        }
    }

    override suspend fun updatePost(post: Post): Resource<Post> {
        // 1. Actualizar localmente primero
        val updatedPost = post.copy(updatedAt = System.currentTimeMillis())
        postDao.updatePost(updatedPost.toEntity())

        // 2. Intentar sincronizar con servidor si hay conexión y tiene serverId
        if (networkMonitor.isOnline() && !post.serverId.isNullOrEmpty()) {
            return try {
                val request = UpdatePostRequest(
                    title = post.title,
                    description = post.description,
                    images = post.images
                )

                val response = apiService.updatePost(post.serverId!!, request)

                if (response.isSuccessful) {
                    val syncedPost = updatedPost.copy(synced = true)
                    postDao.updatePost(syncedPost.toEntity())
                    Resource.Success(syncedPost)
                } else {
                    Resource.Success(updatedPost.copy(synced = false))
                }
            } catch (e: Exception) {
                Resource.Success(updatedPost.copy(synced = false))
            }
        } else {
            return Resource.Success(updatedPost.copy(synced = false))
        }
    }

    override suspend fun deletePost(id: Long): Resource<Unit> {
        // 1. Eliminar localmente
        val post = postDao.getPostById(id).firstOrNull()
        postDao.deletePost(id)

        // 2. Intentar eliminar en servidor si hay conexión y tiene serverId
        if (networkMonitor.isOnline() && !post?.serverId.isNullOrEmpty()) {
            return try {
                val response = apiService.deletePost(post!!.serverId!!)
                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Success(Unit) // Ya eliminado localmente
                }
            } catch (e: Exception) {
                Resource.Success(Unit) // Ya eliminado localmente
            }
        } else {
            return Resource.Success(Unit)
        }
    }

    override suspend fun likePost(postId: Long): Resource<Unit> {
        // Obtener el post para conseguir su serverId
        val post = postDao.getPostById(postId).firstOrNull()
        val serverId = post?.serverId
        
        android.util.Log.d("PostRepository", "likePost - postId local: $postId, serverId: $serverId, post: ${post?.title}")
        
        if (serverId.isNullOrEmpty()) {
            android.util.Log.e("PostRepository", "likePost - Post sin serverId, no se puede votar")
            return Resource.Error("Post no sincronizado aún")
        }
        
        if (networkMonitor.isOnline()) {
            return try {
                android.util.Log.d("PostRepository", "likePost - Enviando voto con serverId: $serverId")
                val response = apiService.votePost(serverId, VoteRequest("like"))
                if (response.isSuccessful && response.body() != null) {
                    val voteResponse = response.body()!!
                    // Actualizar likes en DB local
                    post?.let {
                        val updated = it.copy(
                            likes = voteResponse.likes,
                            dislikes = voteResponse.dislikes
                        )
                        postDao.updatePost(updated)
                    }
                    Resource.Success(Unit)
                } else {
                    Resource.Error(Constants.ErrorMessages.NETWORK_ERROR)
                }
            } catch (e: Exception) {
                Resource.Error(Constants.ErrorMessages.NETWORK_ERROR, e)
            }
        } else {
            return Resource.Error(Constants.ErrorMessages.OFFLINE_ERROR)
        }
    }

    override suspend fun dislikePost(postId: Long): Resource<Unit> {
        // Obtener el post para conseguir su serverId
        val post = postDao.getPostById(postId).firstOrNull()
        val serverId = post?.serverId
        
        android.util.Log.d("PostRepository", "dislikePost - postId local: $postId, serverId: $serverId")
        
        if (serverId.isNullOrEmpty()) {
            android.util.Log.e("PostRepository", "dislikePost - Post sin serverId, no se puede votar")
            return Resource.Error("Post no sincronizado aún")
        }
        
        if (networkMonitor.isOnline()) {
            return try {
                android.util.Log.d("PostRepository", "dislikePost - Enviando voto con serverId: $serverId")
                val response = apiService.votePost(serverId, VoteRequest("dislike"))
                if (response.isSuccessful && response.body() != null) {
                    val voteResponse = response.body()!!
                    // Actualizar dislikes en DB local
                    post?.let {
                        val updated = it.copy(
                            likes = voteResponse.likes,
                            dislikes = voteResponse.dislikes
                        )
                        postDao.updatePost(updated)
                    }
                    Resource.Success(Unit)
                } else {
                    Resource.Error(Constants.ErrorMessages.NETWORK_ERROR)
                }
            } catch (e: Exception) {
                Resource.Error(Constants.ErrorMessages.NETWORK_ERROR, e)
            }
        } else {
            return Resource.Error(Constants.ErrorMessages.OFFLINE_ERROR)
        }
    }

    override suspend fun favoritePost(postId: Long): Resource<Unit> {
        val userId = getCurrentUserId()
        
        // Obtener el post para conseguir su serverId
        val post = postDao.getPostById(postId).firstOrNull()
        val serverId = post?.serverId
        
        android.util.Log.d("PostRepository", "favoritePost - postId local: $postId, serverId: $serverId")
        
        if (serverId.isNullOrEmpty()) {
            android.util.Log.w("PostRepository", "favoritePost - Post sin serverId, solo guardando localmente")
            // Si no tiene serverId, solo guardar localmente
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    userId = userId,
                    postId = postId
                )
            )
            return Resource.Success(Unit)
        }
        
        // 1. Guardar localmente
        favoriteDao.insertFavorite(
            FavoriteEntity(
                userId = userId,
                postId = postId
            )
        )

        // 2. Intentar sincronizar con servidor si hay conexión
        if (networkMonitor.isOnline()) {
            return try {
                android.util.Log.d("PostRepository", "favoritePost - Enviando favorito con serverId: $serverId")
                val response = apiService.favoritePost(serverId, FavoriteRequest(true))
                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Success(Unit) // Ya guardado localmente
                }
            } catch (e: Exception) {
                Resource.Success(Unit) // Ya guardado localmente
            }
        } else {
            return Resource.Success(Unit)
        }
    }

    override suspend fun unfavoritePost(postId: Long): Resource<Unit> {
        val userId = getCurrentUserId()
        
        // Obtener el post para conseguir su serverId
        val post = postDao.getPostById(postId).firstOrNull()
        val serverId = post?.serverId
        
        // 1. Eliminar localmente
        favoriteDao.deleteFavorite(userId, postId)

        // 2. Intentar sincronizar con servidor si hay conexión y tiene serverId
        if (networkMonitor.isOnline() && !serverId.isNullOrEmpty()) {
            return try {
                val response = apiService.favoritePost(serverId, FavoriteRequest(false))
                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    Resource.Success(Unit) // Ya eliminado localmente
                }
            } catch (e: Exception) {
                Resource.Success(Unit) // Ya eliminado localmente
            }
        } else {
            return Resource.Success(Unit)
        }
    }

    override fun searchPosts(query: String): Flow<List<Post>> {
        return sessionManager.userId.flatMapLatest { userId ->
            val favoritesFlow = if (userId != null) {
                favoriteDao.getFavoritesByUserId(userId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList<com.pesgard.social_network_gera.data.local.database.entity.FavoriteEntity>())
            }
            
            combine(
                postDao.searchPosts(query),
                favoritesFlow
            ) { posts, favorites ->
                val favoritePostIds = favorites.map { it.postId }.toSet()
                posts.map { entity ->
                    entity.toDomain().copy(
                        isFavorite = favoritePostIds.contains(entity.id)
                    )
                }
            }
        }
    }

    override fun getFavorites(): Flow<List<Post>> {
        return kotlinx.coroutines.flow.flow {
            val userId = getCurrentUserId()
            val favorites = favoriteDao.getFavoritesByUserId(userId).firstOrNull() ?: emptyList()
            val posts = favorites.mapNotNull { favorite ->
                postDao.getPostById(favorite.postId).firstOrNull()?.toDomain()?.copy(isFavorite = true)
            }
            emit(posts)
        }
    }

    override suspend fun syncPosts(): Resource<Unit> {
        if (!networkMonitor.isOnline()) {
            return Resource.Error(Constants.ErrorMessages.OFFLINE_ERROR)
        }

        return try {
            // Obtener posts no sincronizados
            val unsyncedPosts = postDao.getUnsyncedPosts().firstOrNull() ?: emptyList()

            if (unsyncedPosts.isEmpty()) {
                return Resource.Success(Unit)
            }

            // TODO: Implementar sincronización usando el endpoint /sync
            // Por ahora, intentar crear cada post individualmente
            unsyncedPosts.forEach { entity ->
                val post = entity.toDomain()
                if (post.serverId == null) {
                    createPost(post) // Esto intentará sincronizar
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(Constants.ErrorMessages.NETWORK_ERROR, e)
        }
    }

    override suspend fun refreshPosts(): Resource<Unit> {
        if (!networkMonitor.isOnline()) {
            return Resource.Error(Constants.ErrorMessages.OFFLINE_ERROR)
        }

        return try {
            val response = apiService.getPosts()
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!
                
                android.util.Log.d("PostRepository", "Refrescando ${posts.size} posts desde el servidor")
                
                // Usar una transacción para garantizar atomicidad
                database.withTransaction {
                    // Primero, recopilar todos los usuarios únicos de todos los posts
                    val uniqueUsers = mutableMapOf<String, User>()
                    posts.forEach { dto ->
                        dto.author?.let { authorDto ->
                            if (!uniqueUsers.containsKey(authorDto.id)) {
                                uniqueUsers[authorDto.id] = authorDto.toDomain()
                            }
                        }
                    }
                    
                    // Guardar todos los usuarios primero (garantiza foreign key constraint)
                    uniqueUsers.values.forEach { user ->
                        try {
                            userDao.insertUser(user.toEntity())
                            android.util.Log.d("PostRepository", "Usuario guardado/actualizado: ${user.id}, alias: ${user.alias}")
                        } catch (e: Exception) {
                            // Si el usuario ya existe, continuar (OnConflictStrategy.REPLACE debería manejarlo)
                            android.util.Log.d("PostRepository", "Usuario ${user.id} ya existe o error: ${e.message}")
                        }
                    }
                    
                    // Ahora procesar todos los posts
                    val processedPosts = mutableListOf<String>()
                    posts.forEach { dto ->
                        try {
                            val post = dto.toDomain()
                            val serverId = post.serverId ?: ""
                            
                            if (serverId.isEmpty()) {
                                android.util.Log.w("PostRepository", "Post sin serverId, saltando: ${dto.title}")
                                return@forEach
                            }
                            
                            // Buscar post existente por serverId
                            val existing = postDao.getPostByServerId(serverId)
                            
                            if (existing != null) {
                                // Actualizar post existente manteniendo el ID local
                                val updatedEntity = post.toEntity().copy(
                                    id = existing.id,
                                    serverId = serverId,
                                    synced = true
                                )
                                postDao.updatePost(updatedEntity)
                                android.util.Log.d("PostRepository", "Post actualizado: serverId=$serverId, title=${post.title}")
                            } else {
                                // Insertar nuevo post
                                val entity = post.toEntity().copy(
                                    id = 0, // Room generará un nuevo ID local
                                    serverId = serverId,
                                    synced = true
                                )
                                val insertedId = postDao.insertPost(entity)
                                android.util.Log.d("PostRepository", "NUEVO POST insertado: serverId=$serverId, ID local=$insertedId, title=${post.title}")
                            }
                            
                            processedPosts.add(serverId)
                        } catch (e: Exception) {
                            // Log error pero continuar con el siguiente post
                            android.util.Log.e("PostRepository", "Error guardando post ${dto.id}: ${e.message}", e)
                            e.printStackTrace()
                        }
                    }
                    
                    android.util.Log.d("PostRepository", "Transacción completada. Posts procesados: ${processedPosts.size}/${posts.size}")
                }
                
                android.util.Log.d("PostRepository", "Refresh completado exitosamente. Total posts procesados: ${posts.size}")

                Resource.Success(Unit)
            } else {
                android.util.Log.e("PostRepository", "Error en respuesta del servidor: ${response.code()}")
                Resource.Error(Constants.ErrorMessages.NETWORK_ERROR)
            }
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "Error en refreshPosts: ${e.message}", e)
            Resource.Error(Constants.ErrorMessages.NETWORK_ERROR, e)
        }
    }

    override suspend fun refreshPostById(serverId: String): Resource<Unit> {
        if (!networkMonitor.isOnline()) {
            return Resource.Error(Constants.ErrorMessages.OFFLINE_ERROR)
        }

        return try {
            val response = apiService.getPostById(serverId)
            if (response.isSuccessful && response.body() != null) {
                val postDto = response.body()!!
                
                // Guardar usuario del post si existe
                postDto.author?.let { authorDto ->
                    try {
                        val user = authorDto.toDomain()
                        userDao.insertUser(user.toEntity())
                    } catch (e: Exception) {
                        // Si el usuario ya existe, continuar
                        android.util.Log.d("PostRepository", "Usuario ya existe o error: ${e.message}")
                    }
                }
                
                // Convertir PostDto a Post
                val post = postDto.toDomain()
                
                // Buscar post existente por serverId para mantener ID local
                val existingPost = postDao.getPostByServerId(serverId)
                
                val postEntity = if (existingPost != null) {
                    // Actualizar post existente manteniendo el ID local
                    post.toEntity().copy(
                        id = existingPost.id,
                        serverId = serverId
                    )
                } else {
                    // Insertar nuevo post
                    post.toEntity().copy(serverId = serverId)
                }
                
                // Guardar/actualizar post
                val localPostId = if (existingPost != null) {
                    postDao.updatePost(postEntity)
                    existingPost.id
                } else {
                    postDao.insertPost(postEntity)
                }
                
                // Guardar comentarios del post
                postDto.comments?.let { comments ->
                    saveCommentsFromPost(localPostId, comments)
                }
                
                android.util.Log.d("PostRepository", "Post refrescado: serverId=$serverId, ID local=$localPostId")
                
                Resource.Success(Unit)
            } else {
                Resource.Error(Constants.ErrorMessages.NETWORK_ERROR)
            }
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "Error refrescando post: ${e.message}", e)
            Resource.Error(Constants.ErrorMessages.NETWORK_ERROR, e)
        }
    }
    
    /**
     * Guarda los comentarios de un post (incluyendo replies) en la base de datos local
     * Usa una transacción para asegurar que todos los usuarios existan antes de guardar comentarios
     * @param localPostId ID local del post
     * @param comments Lista de comentarios del PostDto
     */
    private suspend fun saveCommentsFromPost(
        localPostId: Long,
        comments: List<com.pesgard.social_network_gera.data.remote.dto.CommentDto>
    ) {
        // Usar transacción para asegurar atomicidad
        database.withTransaction {
            // Paso 1: Recolectar todos los usuarios únicos de todos los comentarios y replies
            val uniqueUsers = mutableMapOf<String, com.pesgard.social_network_gera.data.remote.dto.UserDto>()
            
            // Recolectar usuarios de comentarios principales
            comments.forEach { commentDto ->
                commentDto.user?.let { userDto ->
                    if (userDto.id.isNotBlank()) {
                        uniqueUsers[userDto.id] = userDto
                    }
                }
                
                // Recolectar usuarios de replies
                commentDto.replies?.forEach { replyDto ->
                    replyDto.user?.let { userDto ->
                        if (userDto.id.isNotBlank()) {
                            uniqueUsers[userDto.id] = userDto
                        }
                    }
                }
            }
            
            // Paso 2: Guardar todos los usuarios primero (con REPLACE para evitar duplicados)
            uniqueUsers.values.forEach { userDto ->
                try {
                    val user = userDto.toDomain()
                    val userEntity = user.toEntity()
                    // Verificar que el usuario tenga un ID válido
                    if (userEntity.id.isNotBlank()) {
                        userDao.insertUser(userEntity)
                        android.util.Log.d("PostRepository", "Usuario guardado: ${userEntity.id} - ${userEntity.alias}")
                    } else {
                        android.util.Log.w("PostRepository", "Usuario sin ID válido, saltando: ${userEntity.alias}")
                    }
                } catch (e: Exception) {
                    // Si el usuario ya existe (OnConflictStrategy.REPLACE), continuar
                    android.util.Log.d("PostRepository", "Usuario ya existe o error al guardar: ${e.message}")
                }
            }
            
            // Paso 3: Guardar comentarios de nivel superior
            comments.forEach { commentDto ->
                // Verificar que el usuario del comentario exista
                val commentUserId = commentDto.user?.id?.takeIf { it.isNotBlank() }
                if (commentUserId == null) {
                    android.util.Log.w("PostRepository", "Comentario sin usuario válido, saltando: ${commentDto.text.take(50)}")
                    return@forEach
                }
                
                // Verificar que el usuario existe en la base de datos
                val userExists = userDao.getUserById(commentUserId).firstOrNull() != null
                if (!userExists) {
                    android.util.Log.e("PostRepository", "Usuario no existe en DB antes de guardar comentario: $commentUserId")
                    // Intentar guardar el usuario nuevamente
                    commentDto.user?.let { userDto ->
                        try {
                            val user = userDto.toDomain()
                            val userEntity = user.toEntity()
                            if (userEntity.id.isNotBlank()) {
                                userDao.insertUser(userEntity)
                                android.util.Log.d("PostRepository", "Usuario guardado después de verificación: ${userEntity.id}")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PostRepository", "Error guardando usuario: ${e.message}")
                        }
                    }
                }
                
                // Convertir comentario a dominio
                val comment = commentDto.toDomain(postId = localPostId, parentCommentId = null)
                
                // Buscar comentario existente por serverId
                val existingComment = commentDao.getCommentByServerId(commentDto.id)
                
                val commentEntity = if (existingComment != null) {
                    // Actualizar comentario existente manteniendo el ID local
                    comment.toEntity().copy(
                        id = existingComment.id,
                        serverId = commentDto.id,
                        postId = localPostId,
                        userId = commentUserId // Asegurar que el userId sea válido
                    )
                } else {
                    // Crear nuevo comentario
                    comment.toEntity().copy(
                        serverId = commentDto.id,
                        postId = localPostId,
                        userId = commentUserId // Asegurar que el userId sea válido
                    )
                }
                
                // Guardar comentario
                val localCommentId = if (existingComment != null) {
                    commentDao.updateComment(commentEntity)
                    existingComment.id
                } else {
                    commentDao.insertComment(commentEntity)
                }
                
                // Paso 4: Guardar replies del comentario
                commentDto.replies?.forEach { replyDto ->
                    // Verificar que el usuario del reply exista
                    val replyUserId = replyDto.user?.id?.takeIf { it.isNotBlank() }
                    if (replyUserId == null) {
                        android.util.Log.w("PostRepository", "Reply sin usuario válido, saltando: ${replyDto.text.take(50)}")
                        return@forEach
                    }
                    
                    // Convertir reply a dominio
                    val reply = replyDto.toDomain(
                        postId = localPostId,
                        parentCommentId = localCommentId // Usar el ID local del comentario padre
                    )
                    
                    // Buscar reply existente por serverId
                    val existingReply = commentDao.getCommentByServerId(replyDto.id)
                    
                    val replyEntity = if (existingReply != null) {
                        // Actualizar reply existente manteniendo el ID local
                        reply.toEntity().copy(
                            id = existingReply.id,
                            serverId = replyDto.id,
                            postId = localPostId,
                            parentCommentId = localCommentId,
                            userId = replyUserId // Asegurar que el userId sea válido
                        )
                    } else {
                        // Crear nuevo reply
                        reply.toEntity().copy(
                            serverId = replyDto.id,
                            postId = localPostId,
                            parentCommentId = localCommentId,
                            userId = replyUserId // Asegurar que el userId sea válido
                        )
                    }
                    
                    // Guardar reply
                    if (existingReply != null) {
                        commentDao.updateComment(replyEntity)
                    } else {
                        commentDao.insertComment(replyEntity)
                    }
                }
            }
        }
        
        android.util.Log.d("PostRepository", "Comentarios guardados: ${comments.size} comentarios para post $localPostId")
    }

    /**
     * Obtiene el ID del usuario actual de forma síncrona
     * WARNING: Solo para uso en funciones suspend
     */
    private suspend fun getCurrentUserId(): String {
        return sessionManager.userId.firstOrNull() ?: ""
    }
}
