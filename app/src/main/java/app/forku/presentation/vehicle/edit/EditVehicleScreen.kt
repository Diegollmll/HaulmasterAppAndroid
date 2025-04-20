package app.forku.presentation.vehicle.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.user.UserRole
import app.forku.presentation.common.components.BaseScreen
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: EditVehicleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // State for text fields - Needs to be initialized from viewModel state after load
    var codename by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bestSuitedFor by remember { mutableStateOf("") }
    var photoModel by remember { mutableStateOf("") }
    var nextService by remember { mutableStateOf("") }

    // State for dropdowns
    var businessExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var energySourceExpanded by remember { mutableStateOf(false) }

    // Effect to initialize text fields once vehicle data is loaded
    LaunchedEffect(state.loadSuccess, state.initialVehicle) {
        if (state.loadSuccess && state.initialVehicle != null) {
            val vehicle = state.initialVehicle!!
            codename = vehicle.codename
            model = vehicle.model
            serialNumber = vehicle.serialNumber
            description = vehicle.description
            bestSuitedFor = vehicle.bestSuitedFor
            photoModel = vehicle.photoModel
            nextService = vehicle.nextService // Assuming this is a String now
            Log.d("EditVehicleScreen", "Fields initialized from loaded vehicle.")
        }
    }

    // Effect to initialize dropdowns with existing data
    LaunchedEffect(state.loadSuccess, state.initialVehicle, state.vehicleTypes) {
        if (state.loadSuccess && state.initialVehicle != null) {
            val vehicle = state.initialVehicle!!
            Log.d("EditVehicleScreen", "Initializing dropdowns for vehicle: ${vehicle.codename}, Business ID: ${vehicle.businessId}")
            
            // Pre-select dropdowns with existing data
            vehicle.type.categoryId?.let { categoryId ->
                state.vehicleCategories.find { it.id == categoryId }?.let { category ->
                    viewModel.selectCategory(category)
                }
            }
            
            // Check if vehicle types are loaded and try to select the vehicle type
            if (state.vehicleTypes.isNotEmpty()) {
                // Find the matching vehicle type in the loaded types
                state.vehicleTypes.find { it.id == vehicle.type.id }?.let { matchingType ->
                    Log.d("EditVehicleScreen", "Found matching vehicle type: ${matchingType.name}")
                    viewModel.selectVehicleType(matchingType)
                } ?: run {
                    Log.d("EditVehicleScreen", "Vehicle type not found in loaded types. Using original type.")
                    viewModel.selectVehicleType(vehicle.type)
                }
            } else {
                Log.d("EditVehicleScreen", "Vehicle types not loaded yet. Using original type.")
                viewModel.selectVehicleType(vehicle.type)
            }

            // Only set business if user is admin and business exists
            if (state.currentUserRole == UserRole.SYSTEM_OWNER || state.currentUserRole == UserRole.SUPERADMIN) {
                vehicle.businessId?.let { businessId ->
                    state.businesses.find { it.id == businessId }?.let { business ->
                        Log.d("EditVehicleScreen", "Setting business: ${business.name}")
                        viewModel.selectBusiness(business.id)
                    } ?: Log.d("EditVehicleScreen", "Business not found for ID: $businessId")
                } ?: Log.d("EditVehicleScreen", "Vehicle has no business ID assigned")
            }
        }
    }

    // Effect to navigate back on successful save
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            navController.navigateUp()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Edit Vehicle" + (state.initialVehicle?.codename?.let { " - $it" } ?: ""),
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Edit Vehicle Details",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Business selection (Optional for Admins)
                    if (state.currentUserRole == UserRole.SYSTEM_OWNER || state.currentUserRole == UserRole.SUPERADMIN) {
                        item {
                            Column {
                                Text(
                                    text = "Business (Optional)",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = businessExpanded,
                                    onExpandedChange = { businessExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = state.businesses.find { it.id == state.selectedBusinessId }?.name ?: "",
                                        onValueChange = {}, // Read-only, selected via dropdown
                                        readOnly = true,
                                        label = { Text("Select Business") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = businessExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = businessExpanded,
                                        onDismissRequest = { businessExpanded = false }
                                    ) {
                                        // Add option for "None" for admins
                                        DropdownMenuItem(
                                            text = { Text("None (Unassigned)") },
                                            onClick = {
                                                viewModel.selectBusiness(null) // Pass null to unassign
                                                businessExpanded = false
                                            }
                                        )
                                        state.businesses.forEach { business ->
                                            DropdownMenuItem(
                                                text = { Text(business.name) },
                                                onClick = {
                                                    viewModel.selectBusiness(business.id)
                                                    businessExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Vehicle Category Selection
                    item {
                         ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.selectedCategory?.name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Vehicle Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                state.vehicleCategories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            viewModel.selectCategory(category)
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Vehicle Type Selection
                    item {
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it }
                        ) {
                             OutlinedTextField(
                                value = state.selectedType?.name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Vehicle Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = state.selectedCategory != null // Enable only after category selected
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                state.vehicleTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            viewModel.selectVehicleType(type)
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Basic Information Fields (SerialNumber, Codename, Model)
                    item {
                         Text(
                            text = "Basic Information",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                     item {
                        OutlinedTextField(
                            value = serialNumber,
                            onValueChange = { serialNumber = it }, // Update local state
                            label = { Text("Serial Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = codename,
                            onValueChange = { codename = it }, // Update local state
                            label = { Text("Vehicle Codename") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it }, // Update local state
                            label = { Text("Model") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Additional Details Fields (Description, Best Suited, Photo)
                     item {
                         Text(
                            text = "Additional Details",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it }, // Update local state
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = bestSuitedFor,
                            onValueChange = { bestSuitedFor = it }, // Update local state
                            label = { Text("Best Suited For") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = photoModel,
                            onValueChange = { photoModel = it }, // Update local state
                            label = { Text("Photo Model") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Energy Source Selection
                    item {
                        ExposedDropdownMenuBox(
                            expanded = energySourceExpanded,
                            onExpandedChange = { energySourceExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = state.selectedEnergySourceEnum?.name ?: "",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Energy Source") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = energySourceExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = energySourceExpanded,
                                onDismissRequest = { energySourceExpanded = false }
                            ) {
                                state.energySourceEnums.forEach { source ->
                                    DropdownMenuItem(
                                        text = { Text(source.name) },
                                        onClick = {
                                            viewModel.selectEnergySource(source)
                                            energySourceExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Next Service Date
                    item {
                        OutlinedTextField(
                            value = nextService,
                            onValueChange = { nextService = it }, // Update local state
                            label = { Text("Next Service Date") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Spacer for button
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Save Button (Floating)
             Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.saveChanges(
                        codename = codename,
                        model = model,
                        serialNumber = serialNumber,
                        description = description,
                        bestSuitedFor = bestSuitedFor,
                        photoModel = photoModel,
                        nextService = nextService
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                enabled = !state.isSaving && state.loadSuccess // Enable after load and if not already saving
                 // TODO: Add more sophisticated validation if needed
            ) {
                Text(if (state.isSaving) "Saving..." else "Save Changes")
            }

            // Error Snackbar
            state.error?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp, start = 16.dp, end = 16.dp),
                    action = {
                        Button(onClick = { viewModel.dismissError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(text = it)
                }
            }
        }
    }
} 