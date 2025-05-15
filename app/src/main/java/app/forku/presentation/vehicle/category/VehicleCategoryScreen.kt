package app.forku.presentation.vehicle.category

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
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.core.auth.TokenErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCategoryScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: VehicleCategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Vehicle Categories",
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
                // Add Category Button
                Button(
                    onClick = { viewModel.showAddDialog() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Category")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categories List
                if (state.categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories found")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.categories) { category ->
                            CategoryCard(
                                category = category,
                                onEdit = { viewModel.showEditDialog(category) },
                                onDelete = { viewModel.deleteCategory(category.id) }
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
                CategoryDialog(
                    category = state.selectedCategory,
                    onDismiss = {
                        if (state.showAddDialog) viewModel.hideAddDialog()
                        else viewModel.hideEditDialog()
                    },
                    onSave = { name ->
                        if (state.showAddDialog) {
                            viewModel.addCategory(name)
                        } else {
                            state.selectedCategory?.let {
                                viewModel.updateCategory(it.copy(name = name))
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: VehicleCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDialog(
    category: VehicleCategory?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (category == null) "Add Category" else "Edit Category")
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text(if (category == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 