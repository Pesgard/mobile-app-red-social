package com.pesgard.social_network_gera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.presentation.navigation.SetupNavigation
import com.pesgard.social_network_gera.sync.SyncManager
import com.pesgard.social_network_gera.ui.theme.ConnectaTheme
import com.pesgard.social_network_gera.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // TEMPORAL: Limpiar WorkManager para resolver estado corrupto
        androidx.work.WorkManager.getInstance(this).cancelAllWork()
        android.util.Log.d("MainActivity", "WorkManager limpiado - reiniciando workers")
        
        // Iniciar sincronización periódica
        syncManager.startPeriodicSync()
        
        // Listener para sincronización inmediata al reconectar
        lifecycleScope.launch {
            var wasOffline = false
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline && wasOffline) {
                    // Se reconectó a internet, sincronizar inmediatamente
                    android.util.Log.d("MainActivity", "Conexión restaurada - Sincronizando inmediatamente")
                    syncManager.syncNow()
                }
                wasOffline = !isOnline
            }
        }
        
        setContent {
            ConnectaTheme {
                SetupNavigation(
                    authRepository = authRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
