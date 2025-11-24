package com.pesgard.social_network_gera.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.Primary

/**
 * Indicador de carga circular
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = ConnectaDimensions.loadingIndicatorSize
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = Primary
        )
    }
}

/**
 * Indicador de carga que ocupa toda la pantalla
 */
@Composable
fun FullScreenLoadingIndicator() {
    LoadingIndicator(
        modifier = Modifier.fillMaxSize()
    )
}


