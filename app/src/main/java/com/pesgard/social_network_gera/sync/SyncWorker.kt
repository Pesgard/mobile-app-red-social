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
            android.util.Log.d("SyncWorker", "=== INICIANDO SINCRONIZACIÓN ===")
            
            // Verificar conexión
            if (!networkMonitor.isOnline()) {
                android.util.Log.w("SyncWorker", "Sin conexión a internet, reintentando más tarde")
                return Result.retry() // Reintentar más tarde
            }
            
            android.util.Log.d("SyncWorker", "Conexión disponible, buscando posts pendientes...")

            // Obtener posts no sincronizados
            val unsyncedPosts = postDao.getUnsyncedPosts().firstOrNull() ?: emptyList()
            
            android.util.Log.d("SyncWorker", "Posts pendientes encontrados: ${unsyncedPosts.size}")

            if (unsyncedPosts.isEmpty()) {
                android.util.Log.d("SyncWorker", "No hay posts pendientes de sincronizar")
                return Result.success() // No hay nada que sincronizar
            }
            
            unsyncedPosts.forEach { post ->
                android.util.Log.d("SyncWorker", "Post pendiente - ID: ${post.id}, Titulo: ${post.title}, Imágenes: ${post.images.length} chars")
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
            android.util.Log.d("SyncWorker", "Enviando ${pendingPosts.size} posts al servidor...")
            android.util.Log.d("SyncWorker", "Request JSON: pendingPosts=${pendingPosts.map { "ID:${it.localId}, Title:${it.title}" }}")
            
            val response = apiService.sync(request)
            
            android.util.Log.d("SyncWorker", "Respuesta del servidor: Code=${response.code()}, Success=${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!
                android.util.Log.d("SyncWorker", "Posts sincronizados exitosamente: ${syncResponse.synced.size}")
                
                // Actualizar posts con serverId
                syncResponse.synced.forEach { syncedPost ->
                    val localId = syncedPost.localId.toLongOrNull()
                    android.util.Log.d("SyncWorker", "Actualizando post local ID: $localId con serverId: ${syncedPost.serverId}")
                    if (localId != null) {
                        postDao.markAsSynced(localId, syncedPost.serverId.toString())
                        android.util.Log.d("SyncWorker", "Post $localId marcado como sincronizado ✅")
                    } else {
                        android.util.Log.e("SyncWorker", "ERROR: No se pudo convertir localId: ${syncedPost.localId}")
                    }
                }

                android.util.Log.d("SyncWorker", "=== SINCRONIZACIÓN COMPLETADA EXITOSAMENTE ===")
                Result.success()
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("SyncWorker", "ERROR en respuesta del servidor: Code=${response.code()}, Error=$errorBody")
                Result.retry() // Reintentar si falla
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "EXCEPCIÓN durante sincronización: ${e.message}", e)
            e.printStackTrace()
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
