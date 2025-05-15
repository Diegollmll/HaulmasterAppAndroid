package app.forku.presentation.vehicle.type

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.vehicle.VehicleType
import app.forku.core.auth.TokenErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTypeScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: VehicleTypeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Vehicle Types",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Category Filter
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.categories.find { it.id == state.selectedCategoryId }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Filter by Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = { }
                    ) {
                        state.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    viewModel.loadTypesByCategory(category.id)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Type Button
                Button(
                    onClick = { viewModel.showAddDialog() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Type")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Types List
                if (state.types.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No types found")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.types) { type ->
                            val category = state.categories.find { it.id == type.VehicleCategoryId }
                            TypeCard(
                                type = type,
                                categoryName = if (category != null) "Category: ${category.name}" else "No Category",
                                onEdit = { viewModel.showEditDialog(type) },
                                onDelete = { viewModel.deleteType(type.Id) }
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Error message
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }

            // Add/Edit Dialog
            if (state.showAddDialog || state.showEditDialog) {
                TypeDialog(
                    type = state.selectedType,
                    categories = state.categories,
                    onDismiss = {
                        if (state.showAddDialog) viewModel.hideAddDialog()
                        else viewModel.hideEditDialog()
                    },
                    onSave = { name, categoryId, requiresCertification ->
                        if (state.showAddDialog) {
                            viewModel.addType(name, categoryId, requiresCertification)
                        } else {
                            state.selectedType?.let {
                                viewModel.updateType(it.copy(
                                    Name = name,
                                    VehicleCategoryId = categoryId,
                                    RequiresCertification = requiresCertification
                                ))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TypeCard(
    type: VehicleType,
    categoryName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = type.Name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2
                    )
                    if (categoryName.isNotBlank()) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            maxLines = 1
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            if (type.RequiresCertification) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text("Requires Certification") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeDialog(
    type: VehicleType?,
    categories: List<app.forku.domain.model.vehicle.VehicleCategory>,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(type?.Name ?: "") }
    var selectedCategoryId by remember { mutableStateOf(type?.VehicleCategoryId ?: categories.firstOrNull()?.id ?: "") }
    var requiresCertification by remember { mutableStateOf(type?.RequiresCertification ?: false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (type == null) "Add Type" else "Edit Type")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Type Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = requiresCertification,
                        onCheckedChange = { requiresCertification = it }
                    )
                    Text("Requires Certification")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedCategoryId, requiresCertification) },
                enabled = name.isNotBlank() && selectedCategoryId.isNotBlank()
            ) {
                Text(if (type == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 