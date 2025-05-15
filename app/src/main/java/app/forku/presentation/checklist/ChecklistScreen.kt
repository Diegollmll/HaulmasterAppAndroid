package app.forku.presentation.checklist

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel = hiltViewModel(),
    navController: NavController,
    onBackPressed: () -> Unit,
    networkManager: NetworkConnectivityManager,
    locationManager: LocationManager,
    imageLoader: ImageLoader,
    tokenErrorHandler: TokenErrorHandler
) {
    Log.d("QRFlow", "ChecklistScreen Composable started")
    
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val showDiscardDialog by viewModel.showDiscardDialog.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Remember the last answered question index and description states
    val lastAnsweredIndex = remember { mutableStateOf(-1) }
    val descriptionStates = remember { mutableMapOf<Int, Boolean>() }
    
    val categoryNameMap by viewModel.categoryNameMap.collectAsState()
    
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
            
            val scrollPosition = baseOffset + (currentIndex + 1) * (questionBaseHeight + 20) + descriptionsHeight
            
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
                when (event.role?.lowercase()) {
                    "admin", "administrator" -> navController.navigate(Screen.AdminDashboard.route)
                    "superadmin" -> navController.navigate(Screen.SuperAdminDashboard.route)
                    "system_owner" -> navController.navigate(Screen.SystemOwnerDashboard.route)
                    "operator" -> navController.navigate(Screen.Dashboard.route)
                    else -> navController.navigate(Screen.Dashboard.route)
                }
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

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        viewModel = viewModel,
        topBarTitle = "Pre-Shift Checklist",
        networkManager = networkManager,
        onRefresh = { viewModel.loadChecklistData() },
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
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
                                    .fillMaxSize()
                                    .padding(padding)
                                    .verticalScroll(scrollState)
                            ) {
                                // Keep VehicleProfileSummary
                                currentState.vehicle?.let { vehicle ->
                                    VehicleProfileSummary(
                                        vehicle = vehicle,
                                        status = vehicle.status,
                                        imageLoader = imageLoader
                                    )
                                }

                                // Add Timer Display
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    // Group items by category
                                    val groupedItems = currentState.checkItems.groupBy { it.category }
                                    var totalIndex = 0
                                    
                                    groupedItems.forEach { (category, items) ->
                                        // Only show the category name, never the ID. If not found, show a placeholder.

                                        val displayCategoryName = categoryNameMap[category] ?: "Unknown Category"
                                        Log.d("ChecklistScreen", "Rendering category: $category, mapped name: $displayCategoryName")
                                        CategoryHeader(
                                            categoryName = displayCategoryName,
                                            modifier = Modifier.padding(bottom = 1.dp)
                                        )

                                        items.forEach { item ->
                                            val currentIndex = totalIndex
                                            
                                            ChecklistQuestionItem(
                                                question = item,
                                                onResponseChanged = { itemId, answer ->
                                                    viewModel.updateItemResponse(itemId, answer)
                                                    lastAnsweredIndex.value = currentIndex
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(100)
                                                        scrollToNextQuestion(currentIndex, currentState.checkItems.size)
                                                    }
                                                },
                                                onDescriptionToggled = { isVisible ->
                                                    descriptionStates[currentIndex] = isVisible
                                                    if (isVisible) {
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(100)
                                                            scrollToNextQuestion(currentIndex, currentState.checkItems.size)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.padding(bottom = 1.dp)
                                            )
                                            totalIndex++
                                        }
                                    }

                                    // Only show submit button when all items are answered
                                    if (currentState.showSubmitButton && currentState.allAnswered) {
                                        Spacer(Modifier.height(4.dp))
                                        Button(
                                            onClick = { viewModel.saveChecklistAnswer() },
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
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Este checklist no tiene preguntas configuradas.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Show modals on top of the content
                if (showConfirmationDialog) {
                    AppModal(
                        onDismiss = { showConfirmationDialog = false },
                        onConfirm = {
                            android.util.Log.d("ChecklistScreen", "Confirming checklist submission...")
                            showConfirmationDialog = false
                            viewModel.submitCheck()
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