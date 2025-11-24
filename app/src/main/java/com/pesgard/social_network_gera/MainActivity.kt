package com.pesgard.social_network_gera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.pesgard.social_network_gera.domain.repository.AuthRepository
import com.pesgard.social_network_gera.presentation.navigation.SetupNavigation
import com.pesgard.social_network_gera.sync.SyncManager
import com.pesgard.social_network_gera.ui.theme.ConnectaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Iniciar sincronización periódica
        syncManager.startPeriodicSync()
        
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
