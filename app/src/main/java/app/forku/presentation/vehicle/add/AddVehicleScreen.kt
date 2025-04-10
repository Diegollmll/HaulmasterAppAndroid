package app.forku.presentation.vehicle.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.EnergySource
import app.forku.domain.model.user.UserRole
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AddVehicleScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: AddVehicleViewModel = hiltViewModel()
) {
    var codename by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var bestSuitedFor by remember { mutableStateOf("") }
    var photoModel by remember { mutableStateOf("") }
    var nextService by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var businessExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var energySourceExpanded by remember { mutableStateOf(false) }
    
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.success) {
        if (state.success) {
            navController.navigateUp()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Add Vehicle",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Add New Vehicle",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Business selection for SYSTEM_OWNER and SUPERADMIN (Optional)
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
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Business") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = businessExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = businessExpanded,
                                    onDismissRequest = { businessExpanded = false }
                                ) {
                                    state.businesses.forEach { business ->
                                        DropdownMenuItem(
                                            onClick = {
                                                viewModel.selectBusiness(business.id)
                                                businessExpanded = false
                                            },
                                            text = { Text(business.name) }
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
                            onValueChange = {},
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
                                    onClick = {
                                        viewModel.selectCategory(category)
                                        categoryExpanded = false
                                    },
                                    text = { Text(category.name) }
                                )
                            }
                        }
                    }
                }

                // Vehicle Type Selection
                item {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.selectedType?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Vehicle Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.vehicleTypes.forEach { type ->
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.selectVehicleType(type)
                                        expanded = false
                                    },
                                    text = { Text(type.name) }
                                )
                            }
                        }
                    }
                }

                // Basic Information
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
                        onValueChange = { serialNumber = it },
                        label = { Text("Serial Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = codename,
                        onValueChange = { codename = it },
                        label = { Text("Vehicle Codename") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Additional Details
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
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = bestSuitedFor,
                        onValueChange = { bestSuitedFor = it },
                        label = { Text("Best Suited For") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = photoModel,
                        onValueChange = { photoModel = it },
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
                            value = state.selectedEnergySource?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Energy Source") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = energySourceExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = energySourceExpanded,
                            onDismissRequest = { energySourceExpanded = false }
                        ) {
                            state.energySources.forEach { source ->
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.selectEnergySource(source)
                                        energySourceExpanded = false
                                    },
                                    text = { Text(source.name) }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = nextService,
                        onValueChange = { nextService = it },
                        label = { Text("Next Service Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }

                item {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            state.selectedType?.let { selectedType ->
                                viewModel.addVehicle(
                                    codename = codename,
                                    model = model,
                                    description = description,
                                    bestSuitedFor = bestSuitedFor,
                                    photoModel = photoModel,
                                    nextService = nextService,
                                    type = selectedType,
                                    serialNumber = serialNumber
                                )
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        enabled = codename.isNotBlank() && 
                                model.isNotBlank() && 
                                description.isNotBlank() &&
                                serialNumber.isNotBlank() &&
                                state.selectedType != null &&
                                state.selectedCategory != null &&
                                state.selectedEnergySource != null &&
                                (state.currentUserRole !in setOf(UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN) || 
                                 state.currentUserRole in setOf(UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN))
                    ) {
                        Text("Add Vehicle")
                    }
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
} 