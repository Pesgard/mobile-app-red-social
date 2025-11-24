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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
                    // Botón para ver borradores
                    IconButton(
                        onClick = { viewModel.toggleDraftsList() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Borradores",
                            tint = MaterialTheme.colorScheme.onSurface
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
                        
                        // Botones de acción: Guardar borrador y Publicar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(ConnectaSpacing.medium)
                        ) {
                            // Botón Guardar borrador
                            OutlinedButton(
                                onClick = { viewModel.saveDraft(context) },
                                enabled = uiState.canSaveDraft && !uiState.isSavingDraft,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (uiState.isSavingDraft) "Guardando..." else if (uiState.editingDraftId != null) "Actualizar" else "Guardar borrador"
                                )
                            }
                            
                            // Botón Publicar
                            Button(
                                onClick = {
                                    viewModel.createPost(context) {
                                        onNavigateBack()
                                    }
                                },
                                enabled = uiState.canSubmit && !uiState.isSubmitting,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (uiState.isSubmitting) "Publicando..." else "Publicar"
                                )
                            }
                        }
                        
                        // Lista de borradores
                        if (uiState.showDraftsList) {
                            Spacer(modifier = Modifier.height(ConnectaSpacing.large))
                            Text(
                                text = "Borradores guardados",
                                style = ConnectaTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = ConnectaSpacing.small)
                            )
                            
                            if (uiState.isLoadingDrafts) {
                                Text("Cargando borradores...")
                            } else if (uiState.drafts.isEmpty()) {
                                Text(
                                    text = "No hay borradores guardados",
                                    style = ConnectaTypography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                uiState.drafts.forEach { draft ->
                                    DraftItem(
                                        draft = draft,
                                        onEditClick = { viewModel.loadDraft(draft.id) },
                                        onDeleteClick = { viewModel.deleteDraft(draft.id) },
                                        onPublishClick = {
                                            viewModel.publishDraft(draft.id, context) {
                                                onNavigateBack()
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente para mostrar un borrador en la lista
 */
@Composable
private fun DraftItem(
    draft: com.pesgard.social_network_gera.domain.model.DraftPost,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ConnectaSpacing.small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ConnectaSpacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (draft.title.isNotEmpty()) {
                        Text(
                            text = draft.title,
                            style = ConnectaTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (draft.description.isNotEmpty()) {
                        Text(
                            text = draft.description,
                            style = ConnectaTypography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = ConnectaSpacing.extraSmall)
                        )
                    }
                    Text(
                        text = "Actualizado: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(draft.updatedAt))}",
                        style = ConnectaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = ConnectaSpacing.small)
                    )
                }
                
                Box {
                    IconButton(
                        onClick = { showMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Publicar") },
                            onClick = {
                                showMenu = false
                                onPublishClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Publish, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Eliminar",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar borrador") },
            text = { Text("¿Estás seguro de que deseas eliminar este borrador?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

