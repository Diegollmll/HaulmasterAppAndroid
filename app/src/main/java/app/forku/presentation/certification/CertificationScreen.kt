package app.forku.presentation.certification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.net.Uri
import coil.compose.AsyncImage
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.DatePickerDialog
import app.forku.core.utils.hideKeyboardOnTapOutside
import app.forku.core.utils.keyboardAwareScroll
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationScreen(
    viewModel: CertificationViewModel = hiltViewModel(),
    certificationId: String? = null,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            viewModel.tempPhotoUri?.let { viewModel.addPhoto(it) }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.createTempPhotoUri(context)?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addPhoto(it) }
    }

    LaunchedEffect(certificationId) {
        if (certificationId != null) {
            viewModel.loadCertification(certificationId)
        }
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            navController.popBackStack()
        }
    }

    if (showIssueDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.updateIssueDate(date)
                showIssueDatePicker = false
            },
            onDismiss = { showIssueDatePicker = false }
        )
    }

    if (showExpiryDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.updateExpiryDate(date)
                showExpiryDatePicker = false
            },
            onDismiss = { showExpiryDatePicker = false }
        )
    }

    // Photo source selection dialog
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { Text("Add Photo") },
            text = { Text("Choose photo source") },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
                        Text("Camera")
                    }
                    TextButton(
                        onClick = {
                            showPhotoSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Text("Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPhotoSourceDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = if (certificationId != null) "Edit Certification" else "Create Certification",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .navigationBarsPadding()
                    .hideKeyboardOnTapOutside()
            ) {
                when {
                    state.isLoading -> LoadingOverlay()
                    state.error != null -> ErrorScreen(
                        message = state.error!!,
                        onRetry = { viewModel.clearError() }
                    )
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                                .keyboardAwareScroll(scrollState)
                        ) {
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = { viewModel.updateName(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.updateDescription(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.issuer,
                                onValueChange = { viewModel.updateIssuer(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Issuer") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showIssueDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.issuedDate ?: "Select Issue Date")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showExpiryDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.expiryDate ?: "Select Expiry Date")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.certificationCode ?: "",
                                onValueChange = { viewModel.updateCertificationCode(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Certification Code") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Vehicle Types Selection
                            Text(
                                text = "Vehicle Types",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            if (state.availableVehicleTypes.isNotEmpty()) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(state.availableVehicleTypes) { vehicleType ->
                                        VehicleTypeChip(
                                            vehicleType = vehicleType,
                                            isSelected = state.selectedVehicleTypeIds.contains(vehicleType.Id),
                                            onToggle = { viewModel.toggleVehicleType(vehicleType.Id) }
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Loading vehicle types...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Photos Section
                            PhotosSection(
                                uploadedPhotos = state.uploadedPhotos,
                                existingMultimedia = state.existingMultimedia,
                                onAddPhoto = { showPhotoSourceDialog = true },
                                onRemovePhoto = { photo -> viewModel.removePhoto(photo) }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.saveCertification() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                enabled = !state.isLoading && state.isValid
                            ) {
                                Text(if (certificationId != null) "Save Changes" else "Create Certification")
                            }

                            if (certificationId != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp)
                                ) {
                                    Text("Debug Info:", style = MaterialTheme.typography.titleSmall)
                                    Text("isMarkedForDeletion: ${state.isMarkedForDeletion}")
                                    Text("isDirty: ${state.isDirty}")
                                    Text("isNew: ${state.isNew}")
                                    Text("internalObjectId: ${state.internalObjectId}")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun PhotosSection(
    uploadedPhotos: List<UploadedPhoto>,
    existingMultimedia: List<app.forku.data.api.dto.certification.CertificationMultimediaDto>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (UploadedPhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Photos (${uploadedPhotos.size + existingMultimedia.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // Show existing multimedia
            items(existingMultimedia) { multimedia ->
                ExistingPhotoItem(multimedia = multimedia)
            }
            
            // Show uploaded photos
            items(uploadedPhotos) { photo ->
                UploadedPhotoItem(
                    photo = photo,
                    onRemove = { onRemovePhoto(photo) }
                )
            }
            
            // Add photo button
            item {
                FilledTonalIconButton(
                    onClick = onAddPhoto,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.Add, "Add photo")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadedPhotoItem(
    photo: UploadedPhoto,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Box {
            AsyncImage(
                model = photo.uri,
                contentDescription = "Certification photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExistingPhotoItem(
    multimedia: app.forku.data.api.dto.certification.CertificationMultimediaDto
) {
    // Get authentication tokens for image loading (following ProfileScreen pattern)
    val context = LocalContext.current
    val authDataStore = remember { app.forku.data.datastore.AuthDataStore(context) }
    var appToken by remember { mutableStateOf<String?>(null) }
    var authToken by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        appToken = authDataStore.getApplicationToken()
        authToken = authDataStore.getAuthenticationToken()
        android.util.Log.d("CertificationScreen", "Loaded tokens for image: appToken=${appToken?.take(10)}, authToken=${authToken?.take(10)}")
    }
    
    Card(
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Use imageUrl if available (calculated field from backend), otherwise construct from multimedia ID
            val baseUrl = multimedia.imageUrl ?: multimedia.id?.let { multimediaId ->
                // Use multimedia ID for URL construction (following VehicleImage pattern)
                "${app.forku.core.Constants.BASE_URL}api/multimedia/file/$multimediaId/Image?t=%LASTEDITEDTIME%"
            }
            
            // Add authentication tokens to the URL (following VehicleImage pattern)
            val authenticatedUrl = if (!baseUrl.isNullOrBlank() && appToken != null && authToken != null) {
                val url = "$baseUrl&_application_token=$appToken&_user_token=$authToken"
                android.util.Log.d("CertificationScreen", "Authenticated image URL: $url")
                url
            } else baseUrl
            
            authenticatedUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Existing certification photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onState = { state ->
                        when (state) {
                            is AsyncImagePainter.State.Error -> {
                                android.util.Log.e("CertificationScreen", "Failed to load image: $url", state.result.throwable)
                            }
                            is AsyncImagePainter.State.Loading -> {
                                android.util.Log.d("CertificationScreen", "Loading image: $url")
                            }
                            is AsyncImagePainter.State.Success -> {
                                android.util.Log.d("CertificationScreen", "Successfully loaded image: $url")
                            }
                            else -> {}
                        }
                    }
                )
            } ?: run {
                // Show placeholder when no image URL is available
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "No image available",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleTypeChip(
    vehicleType: app.forku.domain.model.vehicle.VehicleType,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    FilterChip(
        onClick = onToggle,
        label = { Text(vehicleType.Name) },
        selected = isSelected,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
} 