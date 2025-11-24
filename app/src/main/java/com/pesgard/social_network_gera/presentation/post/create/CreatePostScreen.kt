package com.pesgard.social_network_gera.presentation.post.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.components.ImagePreview
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.ui.theme.Primary

/**
 * Pantalla para crear una nueva publicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Launcher para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(it) }
    }

    // Launcher para seleccionar múltiples imágenes
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.take(5 - uiState.selectedImages.size).forEach { uri ->
            viewModel.addImage(uri)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva Publicación",
                        style = ConnectaTypographyExtensions.appBarTitle,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancelar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createPost(context) {
                                onNavigateBack()
                            }
                        },
                        enabled = uiState.canSubmit && !uiState.isSubmitting
                    ) {
                        Text(
                            text = if (uiState.isSubmitting) "Publicando..." else "Publicar",
                            color = if (uiState.canSubmit && !uiState.isSubmitting) {
                                Primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isSubmitting -> {
                    FullScreenLoadingIndicator()
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(ConnectaSpacing.medium),
                        verticalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium)
                    ) {
                        // Campo de título
                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            label = { Text("Título *") },
                            placeholder = { Text("Escribe un título...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = ConnectaCustomShapes.input
                        )

                        // Campo de descripción
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.updateDescription(it) },
                            label = { Text("Descripción") },
                            placeholder = { Text("Escribe una descripción...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = ConnectaCustomShapes.input
                        )

                        // Preview de imágenes seleccionadas
                        if (uiState.selectedImages.isNotEmpty()) {
                            ImagePreview(
                                images = uiState.selectedImages,
                                onRemoveImage = { index ->
                                    viewModel.removeImage(index)
                                }
                            )
                        }

                        // Botón para agregar imágenes
                        Card(
                            modifier = Modifier.fillMaxWidth()
                                .clickable{
                                    if (uiState.selectedImages.size < 5) {
                                        multipleImagePickerLauncher.launch("image/*")
                                    }
                                },
                            shape = ConnectaCustomShapes.input,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(ConnectaSpacing.medium),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Agregar imagen",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.size(ConnectaSpacing.small))
                                Text(
                                    text = if (uiState.selectedImages.isEmpty()) {
                                        "Agregar imágenes (máx. 5)"
                                    } else {
                                        "Agregar más imágenes (${uiState.selectedImages.size}/5)"
                                    },
                                    style = ConnectaTypography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Mensaje de error
                        uiState.error?.let { error ->
                            Text(
                                text = error,
                                style = ConnectaTypography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = ConnectaSpacing.small)
                            )
                        }
                    }
                }
            }
        }
    }
}

