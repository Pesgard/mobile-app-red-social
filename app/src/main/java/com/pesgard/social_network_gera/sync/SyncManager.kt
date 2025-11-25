package com.pesgard.social_network_gera.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para gestionar la sincronización automática
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context // <--- Add this annotation)
){
    companion object {
        private const val SYNC_WORK_NAME = "sync_work"
        private const val SYNC_INTERVAL_MINUTES = 15L // Sincronizar cada 15 minutos
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Inicia la sincronización periódica automática
     */
    fun startPeriodicSync() {
        // Cancelar trabajo anterior para evitar estados inconsistentes
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,  // REPLACE en lugar de KEEP
            syncWorkRequest
        )
        
        android.util.Log.d("SyncManager", "Sincronización periódica iniciada (cada $SYNC_INTERVAL_MINUTES minutos)")
    }

    /**
     * Cancela la sincronización periódica
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }

    /**
     * Ejecuta una sincronización inmediata (one-time)
     */
    fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncWorkRequest)
    }
}

