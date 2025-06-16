package app.forku.presentation.checklist

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.vehicle.profile.components.VehicleProfileSummary
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.AppModal
import app.forku.presentation.navigation.Screen
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import app.forku.presentation.common.components.LocationPermissionHandler
import app.forku.core.location.LocationManager
import coil.ImageLoader
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.auth.UserRoleManager
import app.forku.domain.model.user.UserRole
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.platform.LocalContext
import app.forku.presentation.checklist.model.ChecklistImage
import app.forku.core.Constants
import app.forku.presentation.common.components.buildAuthenticatedImageLoader
import app.forku.data.datastore.AuthDataStore
import app.forku.core.utils.hideKeyboardOnTapOutside
import android.Manifest
import android.content.pm.PackageManager
import app.forku.presentation.common.components.ForkuButton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = hiltViewModel(),
    navController: NavController,
    onBackPressed: () -> Unit,
    networkManager: NetworkConnectivityManager,
    locationManager: LocationManager,
    imageLoader: ImageLoader,
    tokenErrorHandler: TokenErrorHandler,
    userRole: UserRole
) {
    Log.d("QRFlow", "ChecklistScreen Composable started")
    
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val showDiscardDialog by viewModel.showDiscardDialog.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Remember the last answered question index and description states
    val lastAnsweredIndex = remember { mutableStateOf(-1) }
    val descriptionStates = remember { mutableMapOf<Int, Boolean>() }
    
    val categoryNameMap by viewModel.categoryNameMap.collectAsState()
    val itemImages by viewModel.itemImages.collectAsState(initial = emptyMap())
    val uploadedMultimedia by viewModel.uploadedMultimedia.collectAsState()
    val uploading by viewModel.uploading.collectAsState()
    val uploadErrors by viewModel.uploadErrors.collectAsState()
    val uploadingImages by viewModel.uploadingImages.collectAsState()
    val answeredItemIds by viewModel.answeredItemIds.collectAsState()
    
    // Store the current item ID that is adding an image
    var currentImageItemId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var imageLoader by remember { mutableStateOf<coil.ImageLoader?>(null) }
    LaunchedEffect(Unit) {
        val headers = viewModel.getAuthHeadersFull()
        val authDataStore = AuthDataStore(context)
        val authenticationToken = authDataStore.getAuthenticationToken()
        imageLoader = buildAuthenticatedImageLoader(
            context,
            headers.csrfToken,
            headers.cookie,
            headers.applicationToken,
            authenticationToken
        )
        viewModel.loadChecklistData()
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val itemId = currentImageItemId ?: return@rememberLauncherForActivityResult
            viewModel.tempPhotoUri?.let { viewModel.uploadAndAttachImage(itemId, it) }
        }
        currentImageItemId = null // Clear after use
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val itemId = currentImageItemId ?: return@rememberLauncherForActivityResult
            viewModel.createTempPhotoUri(context)?.let { uri ->
                cameraLauncher.launch(uri)
            }
        } else {
            currentImageItemId = null // Clear if permission denied
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val itemId = currentImageItemId ?: return@rememberLauncherForActivityResult
        uri?.let { viewModel.uploadAndAttachImage(itemId, it) }
        currentImageItemId = null // Clear after use
    }

    // Photo source selection dialog
    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPhotoSourceDialog = false
                currentImageItemId = null // Clear if dialog is dismissed
            },
            title = { Text("Add Photo") },
            text = { Text("Choose photo source") },
            confirmButton = {
                ForkuButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        galleryLauncher.launch("image/*")
                    }
                ) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                ForkuButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        when (PackageManager.PERMISSION_GRANTED) {
                            context.checkSelfPermission(Manifest.permission.CAMERA) -> {
                                currentImageItemId?.let { itemId ->
                                    viewModel.createTempPhotoUri(context)?.let { uri ->
                                        cameraLauncher.launch(uri)
                                    }
                                }
                            }
                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                ) {
                    Text("Camera")
                }
            }
        )
    }

    // Add LocationPermissionHandler
    LocationPermissionHandler(
        onPermissionsGranted = viewModel::onLocationPermissionGranted,
        onPermissionsDenied = viewModel::onLocationPermissionDenied,
        onLocationSettingsDenied = viewModel::onLocationSettingsDenied,
        locationSettingsException = locationManager.locationState.collectAsState().value.locationSettingsException
    )
    
    // Function to scroll to the next question with better calculation
    fun scrollToNextQuestion(currentIndex: Int, totalQuestions: Int) {
        scope.launch {
            // Calculate a more accurate scroll position:
            // - Vehicle summary: ~200dp
            // - Timer display: ~50dp
            // - Each category header: ~50dp
            // - Each question: ~150dp (base height)
            // - Description when visible: ~100dp extra
            // - Padding and spacing: ~20dp per item
            val baseOffset = 250 // Vehicle summary + timer
            val questionBaseHeight = 150 // Base height per question
            
            // Calculate total height of expanded descriptions before current question
            var descriptionsHeight = 0
            for (i in 0..currentIndex) {
                if (descriptionStates[i] == true) {
                    descriptionsHeight += 100 // Height of description when visible
                }
            }
            
            val scrollPosition = baseOffset + (currentIndex + 1) * (questionBaseHeight + 20)
            
            // Add extra offset for category headers (approximate)
            val categoryHeaderOffset = ((currentIndex + 1) / 3) * 50 // Assuming ~3 questions per category
            
            val targetPosition = (scrollPosition + categoryHeaderOffset)
                .coerceAtMost(scrollState.maxValue)
            
            // If it's the last question, scroll to show the submit button
            if (currentIndex == totalQuestions - 1) {
                scrollState.animateScrollTo(scrollState.maxValue)
            } else {
                // Add extra offset to show a bit of the next question
                // If next question has description visible, add more offset
                val nextQuestionDescriptionOffset = if (descriptionStates[currentIndex + 1] == true) 100 else 0
                scrollState.animateScrollTo(targetPosition + 200 + nextQuestionDescriptionOffset)
            }
        }
    }

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        Log.d("QRFlow", "ChecklistScreen - Navigation event received: $navigationEvent")
        val event = navigationEvent
        when (event) {
            is NavigationEvent.Back -> {
                Log.d("QRFlow", "ChecklistScreen - Navigating back")
                navController.popBackStack()
                viewModel.resetNavigation()
            }
            is NavigationEvent.AfterSubmit -> {
                Log.d("QRFlow", "ChecklistScreen - Navigating after submit, role: ${event.role}")
                val userRole = UserRoleManager.fromString(event.role)
                val route = UserRoleManager.getDashboardRoute(userRole)
                navController.navigate(route) {
                    popUpTo(0) { inclusive = true }
                }
                viewModel.resetNavigation()
            }
            is NavigationEvent.VehicleBlocked -> {
                // Mostrar un AlertDialog y redirigir al dashboard
                showBlockedDialog = true
                viewModel.resetNavigation()
            }
            null -> {
                Log.d("QRFlow", "ChecklistScreen - No navigation event")
            }
        }
    }

    // Handle back button press
    BackHandler {
        Log.d("QRFlow", "ChecklistScreen - Back button pressed")
        navController.previousBackStackEntry?.destination?.route?.let { previousRoute ->
            Log.d("QRFlow", "ChecklistScreen - Previous route: $previousRoute")
            navController.popBackStack()
        } ?: run {
            Log.d("QRFlow", "ChecklistScreen - No previous route, navigating to scanner")
            navController.navigate(Screen.QRScanner.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
        viewModel.resetNavigation()
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            title = { Text("Vehicle Blocked") },
            text = { Text("The vehicle has been blocked due to a checklist failure. Please contact a supervisor or select another vehicle.") },
            confirmButton = {
                Button(onClick = {
                    showBlockedDialog = false
                    val route = when (userRole) {
                        UserRole.ADMIN -> Screen.AdminDashboard.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(route) {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Go to Dashboard")
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        viewModel = viewModel,
        topBarTitle = "Pre-Shift Checklist",
        networkManager = networkManager,
        onRefresh = {},
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hideKeyboardOnTapOutside()
                    .padding(padding)
            ) {
                when (val currentState = state) {
                    null -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        if (currentState.isLoading) {
                            LoadingOverlay()
                        }
                        
                        if (currentState.error != null) {
                            ErrorScreen(
                                message = currentState.error,
                                onRetry = { viewModel.loadChecklistData() }
                            )
                        }
                        
                        // Use checkItems instead of checklists
                        if (currentState.checkItems.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .align(Alignment.TopStart)
                            ) {
                                // Keep VehicleProfileSummary
                                currentState.vehicle?.let { vehicle ->
                                    VehicleProfileSummary(
                                        vehicle = vehicle,
                                        status = vehicle.status,
                                        imageLoader = imageLoader ?: LocalContext.current.let { coil.ImageLoader(it) } // fallback al default si es null
                                    )
                                }

                                // Add Timer Display
                                // android.util.Log.d("ChecklistScreen", "[UI] startDateTime=${currentState.startDateTime}, isCompleted=${currentState.isCompleted}, formattedElapsedTime=${currentState.formattedElapsedTime}")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Time Elapsed:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currentState.formattedElapsedTime,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Divider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )

                                // Add padding to the questionary section
                                android.util.Log.d("ChecklistScreen", "[Render] Rendering questionary section with ${currentState.checkItems.size} items")
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    // Group items by category
                                    val groupedItems = currentState.checkItems.groupBy { it.category }
                                    var totalIndex = 0
                                    android.util.Log.d("ChecklistScreen", "[Render] Grouped categories: ${groupedItems.keys}")
                                    groupedItems.forEach { (category, items) ->
                                        val displayCategoryName = categoryNameMap[category] ?: "Unknown Category"
                                        android.util.Log.d("ChecklistScreen", "[Render] Rendering category: $category, mapped name: $displayCategoryName, items: ${items.size}")
                                        CategoryHeader(
                                            categoryName = displayCategoryName,
                                            modifier = Modifier.padding(bottom = 1.dp)
                                        )
                                        items.forEach { item ->
                                            val currentIndex = totalIndex
                                            android.util.Log.d("ChecklistScreen", "[DEBUG] answeredItemIds: $answeredItemIds")
                                            android.util.Log.d("ChecklistScreen", "[DEBUG] uploadedMultimedia keys: ${uploadedMultimedia.keys}")
                                            android.util.Log.d("ChecklistScreen", "[DEBUG] item.id: ${item.id}, answeredItemId: ${answeredItemIds[item.id]}")
                                            android.util.Log.d("ChecklistScreen", "[Render] Rendering item: ${item.id}, question: ${item.question}")
                                            val localImages = itemImages[item.id]?.map { uri ->
                                                ChecklistImage(
                                                    uri = uri,
                                                    isUploading = uploadingImages.contains(uri),
                                                    isUploaded = false,
                                                    backendId = null
                                                )
                                            } ?: emptyList()
                                            android.util.Log.d("ChecklistScreen", "[Render] Local images for item ${item.id}: ${localImages.map { it.uri }}")
                                            val answeredItemId = answeredItemIds[item.id]
                                            android.util.Log.d("appflow", "[UI] itemId=${item.id}, answeredItemId=$answeredItemId, multimediaCount=${answeredItemId?.let { uploadedMultimedia[it] }?.size ?: 0}, multimediaIds=${answeredItemId?.let { uploadedMultimedia[it] }?.map { it.id } ?: emptyList()}")
                                            val multimediaList = answeredItemId?.let { uploadedMultimedia[it] } ?: emptyList()
                                            android.util.Log.d("ChecklistScreen", "[Render] Uploaded images for item ${item.id}: ${multimediaList.map { it.id }}")
                                            android.util.Log.d("ChecklistScreen", "[UI] uploadedMultimedia for item ${item.id}: ${uploadedMultimedia[item.id]}")
                                            val uploadedImages = multimediaList.mapNotNull { media ->
                                                val url = media.imageUrl
                                                    ?: (media.imageInternalName?.let { name ->
                                                        Constants.BASE_URL + "api/multimedia/file/${media.id}/Image?t=%LASTEDITEDTIME%"
                                                    })
                                                if (url == null) {
                                                    android.util.Log.w("ChecklistScreen", "[ImagePreview] No valid URL for media: $media")
                                                }
                                                url?.let {
                                                    android.util.Log.d("ChecklistScreen", "[ImagePreview] Using URL: $it for mediaId: ${media.id}")
                                                    ChecklistImage(
                                                        uri = Uri.parse(it),
                                                        isUploading = false,
                                                        isUploaded = true,
                                                        backendId = media.id
                                                    )
                                                }
                                            } ?: emptyList()
                                            android.util.Log.d("ChecklistScreen", "[UI] Images for item ${item.id} (AnsweredId=$answeredItemId): $uploadedImages")
                                            val isUploading = uploading[item.id] == true
                                            val uploadError = uploadErrors[item.id]
                                            
                                            ChecklistQuestionItem(
                                                question = item,
                                                onResponseChanged = { itemId, answer ->
                                                    android.util.Log.d("ChecklistScreen", "[Event] onResponseChanged for item $itemId: $answer")
                                                    viewModel.updateItemResponse(itemId, answer)
                                                    lastAnsweredIndex.value = currentIndex
                                                },
                                                onDescriptionToggled = { isVisible ->
                                                    android.util.Log.d("ChecklistScreen", "[Event] onDescriptionToggled for item ${item.id}: $isVisible")
                                                    descriptionStates[currentIndex] = isVisible
                                                },
                                                onCommentChanged = { itemId, comment ->
                                                    android.util.Log.d("ChecklistScreen", "[Event] onCommentChanged for item $itemId: $comment")
                                                    viewModel.updateItemComment(itemId, comment)
                                                },
                                                images = localImages + uploadedImages,
                                                onAddImage = {
                                                    android.util.Log.d("ChecklistScreen", "[Event] onAddImage for item ${item.id}")
                                                    currentImageItemId = item.id
                                                    showPhotoSourceDialog = true
                                                },
                                                onRemoveImage = { checklistImage ->
                                                    android.util.Log.d("ChecklistScreen", "[Event] onRemoveImage for item ${item.id}, uploaded: ${checklistImage.isUploaded}, backendId: ${checklistImage.backendId}, uri: ${checklistImage.uri}")
                                                    if (checklistImage.isUploaded && checklistImage.backendId != null) {
                                                        viewModel.removeImageFromBackend(item.id, checklistImage.backendId)
                                                    } else {
                                                        viewModel.onRemoveImage(item.id, checklistImage.uri)
                                                    }
                                                },
                                                modifier = Modifier.padding(bottom = 1.dp),
                                                uploadingImages = uploadingImages,
                                                imageLoader = imageLoader
                                            )
                                            if (isUploading) {
                                                android.util.Log.d("ChecklistScreen", "[Render] Showing LinearProgressIndicator for item ${item.id}")
                                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                            }
                                            uploadError?.let {
                                                android.util.Log.e("ChecklistScreen", "[Render] Upload error for item ${item.id}: $it")
                                                Text("Upload error: $it", color = Color.Red, modifier = Modifier.padding(8.dp))
                                            }
                                            totalIndex++
                                        }
                                    }

                                    // Only show submit button when all items are answered
                                    if (currentState.showSubmitButton && currentState.allAnswered) {
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = { showConfirmationDialog = true },
                                            enabled = currentState.showSubmitButton && currentState.allAnswered,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(56.dp)
                                                .padding(horizontal = 16.dp, vertical = 3.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Text("Save Checklist Progress")
                                        }
                                    }
                                }

                                currentState.message?.let { message ->
                                    Snackbar(
                                        modifier = Modifier
                                            .padding(16.dp)
                                    ) {
                                        Text(message)
                                    }
                                }
                            }
                        } else {
                            // Show a message if no checklist items are available
                            NoChecklistItemsMessage(
                                state = currentState,
                                onContactSupport = { 
                                    // TODO: Implementar navegación a soporte/contacto
                                },
                                onRetry = { viewModel.loadChecklistData() }
                            )
                        }
                    }
                }

                // Show modals on top of the content
                if (showConfirmationDialog) {
                    AppModal(
                        onDismiss = { showConfirmationDialog = false },
                        onConfirm = {
                            // android.util.Log.d("ChecklistScreen", "Confirming checklist submission...")
                            showConfirmationDialog = false
                            viewModel.saveChecklistAnswer()
                        },
                        title = "Submit Checklist",
                        message = "Are you sure you want to submit this checklist?"
                    )
                }

                if (showDiscardDialog) {
                    AppModal(
                        onDismiss = { viewModel.onDiscardDismissed() },
                        onConfirm = { viewModel.onDiscardConfirmed() },
                        title = "Discard Checklist",
                        message = "Are you sure you want to discard this checklist? All progress will be lost."
                    )
                }
            }
        }
    )
}

@Composable
fun CategoryHeader(
    categoryName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = categoryName.replace("_", " ").capitalize(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )
    }
}

@Composable
fun NoChecklistItemsMessage(
    state: ChecklistState,
    onContactSupport: () -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            when {
                state.noCompatibleChecklists -> {
                    // No compatible checklists for this vehicle type
                    androidx.compose.material.icons.Icons.Default.Warning.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = "Warning",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay checklists disponibles",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val vehicleTypeName = state.vehicle?.type?.Name ?: "este tipo de vehículo"
                    
                    Text(
                        text = "No se encontraron checklists configurados para:",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = vehicleTypeName,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Estadísticas del Sistema",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            StatisticRow("Total de checklists", state.totalChecklistsFound.toString())
                            StatisticRow("Específicos para este tipo", state.specificChecklistsFound.toString())
                            StatisticRow("Por defecto/universales", state.defaultChecklistsFound.toString())
                            StatisticRow("Compatibles finales", state.compatibleChecklistsFound.toString())
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ForkuButton(
                            onClick = onContactSupport,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Contactar al Administrador")
                        }
                        
                        OutlinedButton(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                
                state.totalChecklistsFound == 0 -> {
                    // No checklists found at all
                    androidx.compose.material.icons.Icons.Default.Info.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = "Info",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "No hay checklists configurados",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "No se encontraron checklists configurados en el sistema.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ForkuButton(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar")
                    }
                }
                
                else -> {
                    // Generic case - checklist exists but has no questions
                    androidx.compose.material.icons.Icons.Default.CheckCircle.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Checklist sin preguntas",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Este checklist no tiene preguntas configuradas.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ForkuButton(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
    }
}