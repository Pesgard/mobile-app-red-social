package com.pesgard.social_network_gera.presentation.profile.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pesgard.social_network_gera.ui.components.EmptyState
import com.pesgard.social_network_gera.ui.components.FullScreenLoadingIndicator
import com.pesgard.social_network_gera.ui.theme.ConnectaCustomShapes
import com.pesgard.social_network_gera.ui.theme.ConnectaSpacing
import com.pesgard.social_network_gera.ui.theme.ConnectaTypography
import com.pesgard.social_network_gera.ui.theme.ConnectaTypographyExtensions
import com.pesgard.social_network_gera.ui.theme.Primary
import kotlinx.coroutines.launch

/**
 * Pantalla para editar el perfil del usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Launcher para seleccionar imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.selectAvatarImage(it)
            scope.launch {
                viewModel.convertAvatarToBase64(context)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Editar Perfil",
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
                            viewModel.saveProfile(context, onNavigateBack)
                        },
                        enabled = uiState.canSubmit && !uiState.isSubmitting
                    ) {
                        Text(
                            text = if (uiState.isSubmitting) "Guardando..." else "Guardar",
                            color = if (uiState.canSubmit && !uiState.isSubmitting) {
                                Primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.pesgard.social_network_gera.ui.theme.AppBarColor,
                    titleContentColor = com.pesgard.social_network_gera.ui.theme.AppBarTextColor
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    FullScreenLoadingIndicator()
                }
                uiState.user == null -> {
                    EmptyState(
                        title = "No se pudo cargar el perfil",
                        message = "Por favor, intenta nuevamente",
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                        // Información del usuario (solo lectura)
                        uiState.user?.let { user ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(ConnectaSpacing.small)
                            ) {
                                Text(
                                    text = "Información personal",
                                    style = ConnectaTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                // Nombre completo (solo lectura)
                                OutlinedTextField(
                                    value = user.fullName,
                                    onValueChange = { },
                                    label = { Text("Nombre completo") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = ConnectaCustomShapes.input
                                )
                                
                                // Email (solo lectura)
                                OutlinedTextField(
                                    value = user.email,
                                    onValueChange = { },
                                    label = { Text("Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = ConnectaCustomShapes.input
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(ConnectaSpacing.medium))
                            
                            Text(
                                text = "Información editable",
                                style = ConnectaTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Selector de avatar
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(ConnectaSpacing.small)
                        ) {
                            Text(
                                text = "Foto de perfil",
                                style = ConnectaTypography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, Primary, CircleShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.selectedAvatarUri != null || uiState.avatarUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = uiState.selectedAvatarUri ?: uiState.avatarUrl,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    // Icono de cámara superpuesto
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Cambiar foto",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Seleccionar foto",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            
                            Text(
                                text = if (uiState.isConvertingImage) "Procesando imagen..." else "Toca para cambiar",
                                style = ConnectaTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Campo de alias
                        OutlinedTextField(
                            value = uiState.alias,
                            onValueChange = { viewModel.updateAlias(it) },
                            label = { Text("Alias *") },
                            placeholder = { Text("Ingresa tu alias...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = ConnectaCustomShapes.input,
                            supportingText = {
                                Text(
                                    text = "Mínimo 3 caracteres, solo letras, números y guiones bajos",
                                    style = ConnectaTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        // Campo de teléfono
                        OutlinedTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updatePhone(it) },
                            label = { Text("Teléfono") },
                            placeholder = { Text("10 dígitos") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = ConnectaCustomShapes.input,
                            supportingText = {
                                Text(
                                    text = "Formato: 10 dígitos",
                                    style = ConnectaTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        // Campo de dirección
                        OutlinedTextField(
                            value = uiState.address,
                            onValueChange = { viewModel.updateAddress(it) },
                            label = { Text("Dirección") },
                            placeholder = { Text("Ingresa tu dirección...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            shape = ConnectaCustomShapes.input
                        )

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

