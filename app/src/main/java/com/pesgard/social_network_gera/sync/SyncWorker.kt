package com.pesgard.social_network_gera.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pesgard.social_network_gera.data.local.database.dao.PostDao
import com.pesgard.social_network_gera.data.local.database.entity.PostEntity
import com.pesgard.social_network_gera.data.remote.api.ApiService
import com.pesgard.social_network_gera.data.remote.dto.PendingPostDto
import com.pesgard.social_network_gera.data.remote.dto.SyncRequest
import com.pesgard.social_network_gera.util.NetworkMonitor
import com.pesgard.social_network_gera.util.toTimestamp
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Worker para sincronizar datos pendientes en segundo plano
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Verificar conexión
            if (!networkMonitor.isOnline()) {
                return Result.retry() // Reintentar más tarde
            }

            // Obtener posts no sincronizados
            val unsyncedPosts = postDao.getUnsyncedPosts().firstOrNull() ?: emptyList()

            if (unsyncedPosts.isEmpty()) {
                return Result.success() // No hay nada que sincronizar
            }

            // Convertir a DTOs para el request
            val pendingPosts = unsyncedPosts.map { entity ->
                // Convertir imágenes de String a List<String>
                // Las imágenes están almacenadas como JSON string o lista separada por comas
                val imageList = if (entity.images.isNotEmpty()) {
                    try {
                        // Intentar parsear como JSON primero
                        val jsonArray = org.json.JSONArray(entity.images)
                        (0 until jsonArray.length()).map { jsonArray.getString(it) }
                    } catch (e: Exception) {
                        // Si falla, intentar como lista separada por comas
                        entity.images.split(",").filter { it.isNotEmpty() }
                    }
                } else {
                    emptyList()
                }
                
                PendingPostDto(
                    localId = entity.id.toString(),
                    title = entity.title,
                    description = entity.description,
                    images = imageList,
                    createdAt = formatTimestamp(entity.createdAt)
                )
            }

            // Enviar al servidor
            val request = SyncRequest(pendingPosts = pendingPosts)
            val response = apiService.sync(request)

            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!
                
                // Actualizar posts con serverId
                syncResponse.synced.forEach { syncedPost ->
                    val localId = syncedPost.localId.toLongOrNull()
                    if (localId != null) {
                        postDao.markAsSynced(localId, syncedPost.serverId.toString())
                    }
                }

                Result.success()
            } else {
                Result.retry() // Reintentar si falla
            }
        } catch (e: Exception) {
            Result.retry() // Reintentar en caso de error
        }
    }

    /**
     * Formatea un timestamp a formato ISO 8601
     */
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(timestamp))
    }
}
