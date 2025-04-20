package app.forku.presentation.checklist.subcategory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.core.network.NetworkConnectivityManager
import app.forku.data.api.dto.QuestionaryChecklistItemSubcategoryDto
import app.forku.presentation.common.components.BaseScreen
import androidx.navigation.NavController
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun QuestionaryChecklistItemSubcategoryScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    categoryId: String,
    viewModel: QuestionaryChecklistItemSubcategoryViewModel = hiltViewModel()
) {
    // Set categoryId when the screen is first created
    LaunchedEffect(categoryId) {
        viewModel.setCategoryId(categoryId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Show success toast
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Checklist Subcategories",
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
                    // Debug text and refresh button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Debug text for subcategory count
                        Text(
                            text = "Loaded ${uiState.subcategories.size} subcategories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.loadSubcategories() }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Add Subcategory Button
                    Button(
                        onClick = { 
                            viewModel.selectSubcategory(
                                QuestionaryChecklistItemSubcategoryDto(
                                    name = "",
                                    categoryId = categoryId
                                )
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Subcategory")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subcategories List
                    if (uiState.subcategories.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No subcategories found for this category. Add one to get started.",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Quick add test subcategory button
                                Button(
                                    onClick = { 
                                        // Generate a random number to make the test subcategory name unique
                                        val randomNum = (1000..9999).random()
                                        viewModel.createSubcategory(
                                            QuestionaryChecklistItemSubcategoryDto(
                                                name = "Test Subcategory $randomNum",
                                                description = "This is a test subcategory created on ${
                                                    SimpleDateFormat("yyyy-MM-dd HH:mm").format(
                                                        Date()
                                                    )}",
                                                categoryId = categoryId,
                                                priority = 5
                                            )
                                        )
                                    }
                                ) {
                                    Text("Create Test Subcategory")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.subcategories,
                                key = { it.id ?: "new-${it.hashCode()}" } // Ensure proper recomposition with keys
                            ) { subcategory ->
                                val subcategoryId = subcategory.id
                                // Check if ID is valid
                                if (subcategoryId.isNullOrBlank()) {
                                    Log.e("SubcategoryScreen", "Error: Subcategory has null or blank ID: $subcategory")
                                } else {
                                    Log.d("SubcategoryScreen", "Displaying subcategory with ID: $subcategoryId, Name: ${subcategory.name}")
                                }
                                
                                SubcategoryItem(
                                    subcategory = subcategory,
                                    onEdit = { viewModel.selectSubcategory(subcategory) },
                                    onDelete = { 
                                        if (subcategoryId.isNullOrBlank()) {
                                            Log.e("SubcategoryScreen", "Cannot delete subcategory with null ID")
                                        } else {
                                            Log.d("SubcategoryScreen", "Requesting deletion for subcategory: ID=$subcategoryId, Name=${subcategory.name}")
                                            viewModel.deleteSubcategory(subcategory)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Error Dialog
            if (uiState.error != null) {
                Log.e("SubcategoryScreen", "Error state: ${uiState.error}")
                val errorMessage = uiState.error // Capture the error in a local variable
                val context = LocalContext.current // Access LocalContext within the Composable
                
                LaunchedEffect(errorMessage) {
                    Toast.makeText(
                        context,
                        errorMessage,
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Clear error after showing toast, but with a delay to ensure visibility
                    delay(3500) // Keep error message visible longer (3.5 seconds)
                    viewModel.clearError()
                }
            }

            // Add/Edit Dialog
            if (uiState.isEditMode) {
                SubcategoryFormDialog(
                    subcategory = uiState.selectedSubcategory ?: QuestionaryChecklistItemSubcategoryDto(name = "", categoryId = categoryId),
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { subcategory ->
                        if (subcategory.id == null) {
                            viewModel.createSubcategory(subcategory)
                        } else {
                            viewModel.updateSubcategory(subcategory)
                        }
                        viewModel.clearSelection()
                    }
                )
            }
        }
    }
}

@Composable
private fun SubcategoryItem(
    subcategory: QuestionaryChecklistItemSubcategoryDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Add detailed logging about the subcategory
    LaunchedEffect(subcategory) {
        Log.d("SubcategoryItem", "Displaying subcategory: ID=${subcategory.id}, Name=${subcategory.name}, CategoryID=${subcategory.categoryId}")
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subcategory.name,
                    style = MaterialTheme.typography.titleMedium
                )
                subcategory.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Priority: ${subcategory.priority} (ID: ${subcategory.id})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { 
                        Log.d("SubcategoryItem", "Delete button clicked for ID: ${subcategory.id}")
                        showDeleteConfirmation = true 
                    }
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = { 
                Column {
                    Text("Are you sure you want to delete '${subcategory.name}'?") 
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Subcategory ID: ${subcategory.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (subcategory.id.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Warning: This subcategory has no ID and may fail to delete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d("SubcategoryItem", "Delete confirmed for ID: ${subcategory.id}")
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SubcategoryFormDialog(
    subcategory: QuestionaryChecklistItemSubcategoryDto,
    onDismiss: () -> Unit,
    onSave: (QuestionaryChecklistItemSubcategoryDto) -> Unit
) {
    var name by remember { mutableStateOf(subcategory.name) }
    var description by remember { mutableStateOf(subcategory.description ?: "") }
    var priorityText by remember { mutableStateOf(subcategory.priority.toString()) }
    var priorityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subcategory.id == null) "Add Subcategory" else "Edit Subcategory") },
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priorityText,
                    onValueChange = { 
                        priorityText = it
                        priorityError = it.toIntOrNull() == null
                    },
                    label = { Text("Priority (1-10)") },
                    isError = priorityError,
                    supportingText = {
                        if (priorityError) {
                            Text("Please enter a valid number")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priority = priorityText.toIntOrNull() ?: subcategory.priority
                    onSave(
                        subcategory.copy(
                            name = name, 
                            description = description.ifBlank { null },
                            priority = priority.coerceIn(1, 10)
                        )
                    )
                },
                enabled = name.isNotBlank() && !priorityError
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