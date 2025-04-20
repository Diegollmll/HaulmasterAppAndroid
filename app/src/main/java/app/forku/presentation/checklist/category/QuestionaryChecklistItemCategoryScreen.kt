package app.forku.presentation.checklist.category

import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.core.network.NetworkConnectivityManager
import app.forku.data.api.dto.QuestionaryChecklistItemCategoryDto
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.navigation.NavController

@Composable
fun QuestionaryChecklistItemCategoryScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: QuestionaryChecklistItemCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Questionary Checklist Categories",
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Add Category Button
                    Button(
                        onClick = { viewModel.selectCategory(QuestionaryChecklistItemCategoryDto(name = "")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Category")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categories List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.categories) { category ->
                            CategoryItem(
                                category = category,
                                onEdit = { viewModel.selectCategory(category) },
                                onDelete = { viewModel.deleteCategory(category.id!!) },
                                navController = navController
                            )
                        }
                    }
                }
            }

            // Error Dialog
            uiState.error?.let { error ->
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    title = { Text("Error") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                )
            }

            // Add/Edit Dialog
            if (uiState.isEditMode) {
                CategoryFormDialog(
                    category = uiState.selectedCategory ?: QuestionaryChecklistItemCategoryDto(name = ""),
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { category ->
                        if (category.id == null) {
                            viewModel.createCategory(category)
                        } else {
                            viewModel.updateCategory(category)
                        }
                        viewModel.clearSelection()
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: QuestionaryChecklistItemCategoryDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    category.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            
            // Subtle divider
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            // More subtle subcategories button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        category.id?.let { categoryId ->
                            navController.navigate(Screen.QuestionaryChecklistItemSubcategory.createRoute(categoryId))
                        }
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.List, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Manage Subcategories", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.ArrowForward, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CategoryFormDialog(
    category: QuestionaryChecklistItemCategoryDto,
    onDismiss: () -> Unit,
    onSave: (QuestionaryChecklistItemCategoryDto) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var description by remember { mutableStateOf(category.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category.id == null) "Add Category" else "Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(category.copy(name = name, description = description))
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 