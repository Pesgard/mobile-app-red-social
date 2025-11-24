package com.pesgard.social_network_gera.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaDimensions
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.ui.theme.Primary

/**
 * Botón primario reutilizable con el estilo de la app
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ConnectaDimensions.buttonHeightMedium),
        enabled = enabled && !isLoading,
        shape = ConnectaCustomShapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    ) {
        if (isLoading) {
            LoadingIndicator(
                modifier = Modifier.size(20.dp),
                size = 20.dp
            )
        } else {
            Text(
                text = text,
                style = ConnectaTypographyExtensions.buttonText,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

