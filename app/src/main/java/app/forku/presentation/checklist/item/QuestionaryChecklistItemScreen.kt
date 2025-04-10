package app.forku.presentation.checklist.item

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.core.network.NetworkConnectivityManager
import app.forku.data.model.QuestionaryChecklistItemDto
import app.forku.presentation.common.components.BaseScreen
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun QuestionaryChecklistItemScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    checklistId: String,
    questionaryTitle: String? = null,
    viewModel: QuestionaryChecklistItemViewModel = hiltViewModel()
) {
    // Set checklistId when the screen is first created
    LaunchedEffect(checklistId) {
        viewModel.setChecklistId(checklistId)
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

    // Show error toast
    if (uiState.error != null) {
        Log.e("QuestionaryItemScreen", "Error state: ${uiState.error}")
        val errorMessage = uiState.error
        val errorContext = LocalContext.current
        
        LaunchedEffect(errorMessage) {
            Toast.makeText(
                errorContext,
                errorMessage,
                Toast.LENGTH_LONG
            ).show()
            
            delay(3500)
            viewModel.clearError()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = questionaryTitle?.let { "Items: $it" } ?: "Questionary Items",
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
                        // Debug text
                        Text(
                            text = "Loaded ${uiState.items.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.loadItems() }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Add Item Button
                    Button(
                        onClick = { 
                            val emptyItem = viewModel.createEmptyItem()
                            viewModel.selectItem(emptyItem)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Questionary Item")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Items List
                    if (uiState.items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No items found for this questionary. Add one to get started.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { 
                                        val testItem = viewModel.createEmptyItem().copy(
                                            question = "Test Question ${(1000..9999).random()}",
                                            description = "This is a test question created on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}"
                                        )
                                        viewModel.createItem(testItem)
                                    }
                                ) {
                                    Text("Create Test Item")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = uiState.items,
                                key = { it.id ?: "new-${it.hashCode()}" }
                            ) { item ->
                                QuestionaryChecklistItemCard(
                                    item = item,
                                    onEdit = { viewModel.selectItem(item) },
                                    onDelete = { viewModel.deleteItem(item) }
                                )
                            }
                        }
                    }
                }
            }

            // Add/Edit Dialog
            if (uiState.isEditMode) {
                QuestionaryChecklistItemFormDialog(
                    item = uiState.selectedItem ?: viewModel.createEmptyItem(),
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { item ->
                        if (item.id == null) {
                            viewModel.createItem(item)
                        } else {
                            viewModel.updateItem(item)
                        }
                        viewModel.clearSelection()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuestionaryChecklistItemCard(
    item: QuestionaryChecklistItemDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    val criticalityColor = if (item.isCritical) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    
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
                // Question Text and Metadata
                Column(modifier = Modifier.weight(1f)) {
                    // Criticality badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = criticalityColor.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (item.isCritical) "CRITICAL" else "STANDARD",
                                style = MaterialTheme.typography.labelSmall,
                                color = criticalityColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Text(
                            text = "Group ${item.rotationGroup} • Position ${item.position}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Question
                    Text(
                        text = item.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Description if available
                    item.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // ID for debugging
                    if (item.id != null) {
                        Text(
                            text = "ID: ${item.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                // Action buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
                    Text("Are you sure you want to delete this question?") 
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "\"${item.question}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (item.id != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Item ID: ${item.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
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
private fun QuestionaryChecklistItemFormDialog(
    item: QuestionaryChecklistItemDto,
    onDismiss: () -> Unit,
    onSave: (QuestionaryChecklistItemDto) -> Unit
) {
    var question by remember { mutableStateOf(item.question) }
    var description by remember { mutableStateOf(item.description ?: "") }
    var isCritical by remember { mutableStateOf(item.isCritical) }
    var rotationGroup by remember { mutableStateOf(item.rotationGroup.toString()) }
    var position by remember { mutableStateOf(item.position.toString()) }
    var isActive by remember { mutableStateOf(item.isActive) }
    var energySource by remember { mutableStateOf(item.energySource.joinToString(", ")) }
    var vehicleType by remember { mutableStateOf(item.vehicleType.joinToString(", ")) }
    var component by remember { mutableStateOf(item.component ?: "") }
    
    // Validation
    val isQuestionValid = question.isNotBlank()
    val isRotationGroupValid = rotationGroup.toIntOrNull() != null
    val isPositionValid = position.toIntOrNull() != null
    val isFormValid = isQuestionValid && isRotationGroupValid && isPositionValid
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item.id == null) "Create Question Item" else "Edit Question Item") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Question
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isQuestionValid,
                    supportingText = {
                        if (!isQuestionValid) {
                            Text("Question cannot be empty")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Criticality
                Text(
                    text = "Criticality",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !isCritical,
                        onClick = { isCritical = false }
                    )
                    Text(
                        text = "Standard",
                        modifier = Modifier.clickable { isCritical = false }
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    RadioButton(
                        selected = isCritical,
                        onClick = { isCritical = true }
                    )
                    Text(
                        text = "Critical",
                        modifier = Modifier.clickable { isCritical = true }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rotation Group
                OutlinedTextField(
                    value = rotationGroup,
                    onValueChange = { rotationGroup = it },
                    label = { Text("Rotation Group") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isRotationGroupValid,
                    supportingText = {
                        if (!isRotationGroupValid) {
                            Text("Please enter a valid number")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Position
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Position") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isPositionValid,
                    supportingText = {
                        if (!isPositionValid) {
                            Text("Please enter a valid number")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Active Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text(
                        text = "Active",
                        modifier = Modifier.clickable { isActive = !isActive }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Energy Source
                OutlinedTextField(
                    value = energySource,
                    onValueChange = { energySource = it },
                    label = { Text("Energy Source") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vehicle Type
                OutlinedTextField(
                    value = vehicleType,
                    onValueChange = { vehicleType = it },
                    label = { Text("Vehicle Type") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Component
                OutlinedTextField(
                    value = component,
                    onValueChange = { component = it },
                    label = { Text("Component") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Update the item with edited values
                    val updatedItem = item.copy(
                        question = question,
                        description = description.ifBlank { null },
                        isCritical = isCritical,
                        rotationGroup = rotationGroup.toIntOrNull() ?: 1,
                        position = position.toIntOrNull() ?: 0,
                        isActive = isActive,
                        energySource = energySource.split(", ").map { it.trim() },
                        vehicleType = vehicleType.split(", ").map { it.trim() },
                        component = component.ifBlank { null }
                    )
                    onSave(updatedItem)
                },
                enabled = isFormValid
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