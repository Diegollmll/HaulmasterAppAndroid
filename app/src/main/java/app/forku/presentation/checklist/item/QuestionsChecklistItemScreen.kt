package app.forku.presentation.checklist.item

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.ChecklistItemCategory
import app.forku.domain.model.checklist.ChecklistItemSubcategory
import app.forku.presentation.common.components.BaseScreen
import androidx.navigation.NavController
import app.forku.data.api.dto.vehicle.VehicleComponentDto
import kotlinx.coroutines.delay
import app.forku.core.auth.TokenErrorHandler
import app.forku.domain.model.checklist.Answer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Composable
fun QuestionsChecklistItemScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    checklistId: String,
    isEditable: Boolean = true,
    questionnaireTitle: String? = null,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: QuestionsChecklistItemViewModel = hiltViewModel()
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
        Log.e("QuestionsItemScreen", "Error state: ${uiState.error}")
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
        topBarTitle = questionnaireTitle?.let { "Items: $it" } ?: "Checklist Items",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
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
                    
                    // Add Item Button (only if editable)
                    if (isEditable) {
                    Button(
                        onClick = { 
                            val emptyItem = viewModel.createEmptyItem()
                            viewModel.selectItem(emptyItem)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Checklist Item")
                        }
                    } else {
                        // Show read-only indicator
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Read Only",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "This is a default checklist - items are read-only",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
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
                                    text = "No items found for this checklist. Add one to get started.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = uiState.items,
                                key = { it.id.ifEmpty { "new-${it.hashCode()}" } }
                            ) { item ->
                                ChecklistItemCard(
                                    item = item,
                                    isEditable = isEditable,
                                    onEdit = { viewModel.selectItem(item) },
                                    onDelete = { viewModel.deleteItem(item) },
                                    createdByUserName = viewModel.getUserName(item.goUserId)
                                )
                            }
                        }
                    }
                }
            }

            // Add/Edit Dialog (only if editable)
            if (uiState.isEditMode && isEditable) {
                ChecklistItemFormDialog(
                    item = uiState.selectedItem ?: viewModel.createEmptyItem(),
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { item: ChecklistItem ->
                        if (item.id.isEmpty()) {
                            viewModel.createItem(item)
                        } else {
                            viewModel.updateItem(item)
                        }
                        viewModel.clearSelection()
                    },
                    categories = uiState.availableCategories,
                    subcategories = uiState.availableSubcategories,
                    onCategorySelected = { categoryId ->
                        viewModel.loadSubcategoriesForCategory(categoryId)
                    },
                    availableVehicleTypes = uiState.availableVehicleTypes, // ✅ New: Pass available vehicle types
                    selectedQuestionVehicleTypeIds = uiState.selectedQuestionVehicleTypeIds // ✅ New: Pass pre-selected vehicle type IDs
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistItemCard(
    item: ChecklistItem,
    isEditable: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    createdByUserName: String? = null // ✅ New: Display creator name
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (isEditable) modifier.clickable { onEdit() } else modifier
            },
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
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        ) {
                            Text(
                        text = item.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (item.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.isCritical) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                    Text(
                                    text = "Critical",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                        Text(
                                text = "Group ${item.rotationGroup}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                    
                    // Show creator information if available
                    if (!createdByUserName.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Creator",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Created by $createdByUserName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                if (isEditable) {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                        IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                } else {
                    // Show read-only indicator for individual items
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Read Only",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = "Read Only",
                            style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChecklistItemFormDialog(
    item: ChecklistItem,
    onDismiss: () -> Unit,
    onSave: (ChecklistItem) -> Unit,
    categories: List<ChecklistItemCategory>,
    subcategories: List<ChecklistItemSubcategory>,
    onCategorySelected: (String) -> Unit,
    availableComponents: List<app.forku.domain.model.vehicle.VehicleComponentEnum> = app.forku.domain.model.vehicle.VehicleComponentEnum.values().toList(),
    availableVehicleTypes: List<app.forku.domain.model.vehicle.VehicleType> = emptyList(), // ✅ New: Available vehicle types
    selectedQuestionVehicleTypeIds: List<String> = emptyList() // ✅ New: Pre-selected vehicle type IDs
) {
    var editedItem by remember { mutableStateOf(item) }
    var expectedAnswerExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var componentExpanded by remember { mutableStateOf(false) }
    var energySourceExpanded by remember { mutableStateOf(false) }
    var vehicleTypeExpanded by remember { mutableStateOf(false) } // ✅ New: Vehicle type dialog state
    var validationError by remember { mutableStateOf<String?>(null) } // ✅ New: Validation error state
    
    // ✅ Initialize with proper state management
    var selectedVehicleTypeIds by remember(key1 = item.id, key2 = selectedQuestionVehicleTypeIds.hashCode()) { 
        mutableStateOf(
            if (item.id.isNotEmpty() && selectedQuestionVehicleTypeIds.isNotEmpty()) {
                selectedQuestionVehicleTypeIds.toSet()
            } else {
                item.supportedVehicleTypeIds
            }
        ) 
    }
    
    // Only sync once when the dialog opens for existing items, don't override user selections
    LaunchedEffect(item.id) {
        if (item.id.isNotEmpty() && selectedQuestionVehicleTypeIds.isNotEmpty() && selectedVehicleTypeIds.isEmpty()) {
            Log.d("ChecklistItemFormDialog", "Initial sync of vehicle types from backend: $selectedQuestionVehicleTypeIds")
            selectedVehicleTypeIds = selectedQuestionVehicleTypeIds.toSet()
        }
    }
    
    // Debug logging for state changes
    LaunchedEffect(selectedVehicleTypeIds) {
        Log.d("ChecklistItemFormDialog", "🔄 [STATE-CHANGE] selectedVehicleTypeIds: $selectedVehicleTypeIds (count: ${selectedVehicleTypeIds.size})")
        selectedVehicleTypeIds.forEach { id ->
            val name = availableVehicleTypes.find { it.Id == id }?.Name ?: "Unknown"
            Log.d("ChecklistItemFormDialog", "🔄 [STATE-CHANGE] Selected: $id → $name")
        }
    }
    
    // Debug logging when dialog closes
    LaunchedEffect(vehicleTypeExpanded) {
        if (!vehicleTypeExpanded) {
            Log.d("ChecklistItemFormDialog", "🎬 [DIALOG-CLOSED] Checking final state...")
            Log.d("ChecklistItemFormDialog", "🎬 [DIALOG-CLOSED] selectedVehicleTypeIds: $selectedVehicleTypeIds (count: ${selectedVehicleTypeIds.size})")
            selectedVehicleTypeIds.forEach { id ->
                val name = availableVehicleTypes.find { it.Id == id }?.Name ?: "Unknown"
                Log.d("ChecklistItemFormDialog", "🎬 [DIALOG-CLOSED] Should display: $id → $name")
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item.id.isEmpty()) "Add Checklist Item" else "Edit Checklist Item"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Question field
                OutlinedTextField(
                    value = editedItem.question,
                    onValueChange = { editedItem = editedItem.copy(question = it) },
                    label = { Text("Question") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = false,
                    minLines = 2
                )
                
                // Description field
                OutlinedTextField(
                    value = editedItem.description,
                    onValueChange = { editedItem = editedItem.copy(description = it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = false
                )

                // ChecklistItemCategory dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == editedItem.category }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("ChecklistItemCategory") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    editedItem = editedItem.copy(category = category.id)
                                    categoryExpanded = false
                                    // Load subcategories when category is selected
                                    onCategorySelected(category.id)
                                }
                            )
                        }
                    }
                }
                
                // ChecklistItemSubcategory dropdown
                ExposedDropdownMenuBox(
                    expanded = subcategoryExpanded,
                    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = subcategories.find { it.id == editedItem.subCategory }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("ChecklistItemSubcategory") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = subcategoryExpanded,
                        onDismissRequest = { subcategoryExpanded = false }
                    ) {
                        subcategories.forEach { subcategory ->
                            DropdownMenuItem(
                                text = { Text(subcategory.name) },
                                onClick = {
                                    editedItem = editedItem.copy(subCategory = subcategory.id)
                                    subcategoryExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // EnergySource selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                Text(
                            text = "EnergySource",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                )
                
                        // Energy Source chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            editedItem.energySourceEnum.forEach { energySource ->
                                FilterChip(
                                    onClick = { 
                                        // Remove this energy source
                                        editedItem = editedItem.copy(
                                            energySourceEnum = editedItem.energySourceEnum - energySource
                                        )
                                    },
                                    label = { Text(energySource.name) },
                                    selected = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                        
                        // Add energy source button
                        TextButton(
                            onClick = { energySourceExpanded = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Energy Source")
                        }
                    }
                }

                // All Vehicle Types Enabled checkbox - MOVED BEFORE vehicle types selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = editedItem.allVehicleTypesEnabled,
                            onCheckedChange = { isEnabled ->
                                editedItem = editedItem.copy(allVehicleTypesEnabled = isEnabled)
                                // If enabling all vehicle types, clear specific selections
                                if (isEnabled) {
                                    selectedVehicleTypeIds = emptySet()
                                    validationError = null // Clear validation error when enabling all types
                                } else {
                                    // When disabling all vehicle types, show validation if no types selected
                                    if (selectedVehicleTypeIds.isEmpty()) {
                                        validationError = "When 'All Vehicle Types' is disabled, you must select at least one vehicle type."
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Apply to All Vehicle Types",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "When enabled, this question will apply to all vehicle types",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Vehicle Types selection
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .alpha(if (editedItem.allVehicleTypesEnabled) 0.5f else 1f),
                    colors = CardDefaults.cardColors(
                        containerColor = if (editedItem.allVehicleTypesEnabled) 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (editedItem.allVehicleTypesEnabled) 
                                "Supported Vehicle Types (Disabled - All Types Enabled)" 
                            else 
                                "Supported Vehicle Types",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = if (editedItem.allVehicleTypesEnabled) 
                                MaterialTheme.colorScheme.outline 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Vehicle Type chips - Display selected types
                        // Debug: Log the current state
                        Log.d("ChecklistItemFormDialog", "🔍 [RENDER] selectedVehicleTypeIds.size: ${selectedVehicleTypeIds.size}")
                        Log.d("ChecklistItemFormDialog", "🔍 [RENDER] selectedVehicleTypeIds: $selectedVehicleTypeIds")
                        Log.d("ChecklistItemFormDialog", "🔍 [RENDER] availableVehicleTypes.size: ${availableVehicleTypes.size}")
                        
                        // Chips container with proper layout
                        if (selectedVehicleTypeIds.isNotEmpty()) {
                            Log.d("ChecklistItemFormDialog", "🎨 [FLOWROW] Creating auto-wrapping container for ${selectedVehicleTypeIds.size} items")
                            
                            // Container card for better visibility
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                // Use FlowRow for automatic wrapping - no scroll needed!
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedVehicleTypeIds.forEachIndexed { index, vehicleTypeId ->
                                        val vehicleTypeName = availableVehicleTypes.find { it.Id == vehicleTypeId }?.Name ?: "Unknown ($vehicleTypeId)"
                                        Log.d("ChecklistItemFormDialog", "🎯 [CHIP-$index] Rendering: $vehicleTypeId → $vehicleTypeName")
                                        
                                        FilterChip(
                                            onClick = { 
                                                Log.d("ChecklistItemFormDialog", "❌ [REMOVE] Removing: $vehicleTypeId")
                                                selectedVehicleTypeIds = selectedVehicleTypeIds - vehicleTypeId
                                                Log.d("ChecklistItemFormDialog", "✅ [REMOVE] New state: $selectedVehicleTypeIds")
                                                
                                                // Show validation error if this was the last vehicle type and allVehicleTypesEnabled is false
                                                if (!editedItem.allVehicleTypesEnabled && selectedVehicleTypeIds.size == 1) {
                                                    validationError = "When 'All Vehicle Types' is disabled, you must select at least one vehicle type."
                                                } else if (selectedVehicleTypeIds.size > 1) {
                                                    validationError = null // Clear error if there are still types selected
                                                }
                                            },
                                            label = { 
                                                Text(
                                                    text = vehicleTypeName,
                                                    maxLines = 1,
                                                    style = MaterialTheme.typography.labelMedium
                                                ) 
                                            },
                                            selected = true,
                                            trailingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Show message when no vehicle types are selected
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = if (editedItem.allVehicleTypesEnabled) 
                                        "All vehicle types enabled - question will apply to all types" 
                                    else 
                                        "No vehicle types selected - this question will apply to all vehicle types",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Add spacing between chips and button
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Add vehicle type button
                        TextButton(
                            onClick = { 
                                if (!editedItem.allVehicleTypesEnabled) {
                                    Log.d("ChecklistItemFormDialog", "🔧 [BUTTON] Opening vehicle type dialog")
                                    Log.d("ChecklistItemFormDialog", "🔧 [BUTTON] Current selected: $selectedVehicleTypeIds")
                                    vehicleTypeExpanded = true 
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !editedItem.allVehicleTypesEnabled
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Vehicle Type")
                        }
                    }
                }

                // ✅ VALIDATION ERROR DISPLAY
                validationError?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Vehicle Component dropdown
                ExposedDropdownMenuBox(
                    expanded = componentExpanded,
                    onExpandedChange = { componentExpanded = !componentExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                OutlinedTextField(
                        value = editedItem.component.displayName,
                        onValueChange = { },
                    readOnly = true,
                        label = { Text("Vehicle Component") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = componentExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = componentExpanded,
                        onDismissRequest = { componentExpanded = false }
                    ) {
                        availableComponents.forEach { component ->
                            DropdownMenuItem(
                                text = { Text(component.displayName) },
                                onClick = {
                                    editedItem = editedItem.copy(component = component)
                                    componentExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Critical checkbox
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                        checked = editedItem.isCritical,
                        onCheckedChange = { editedItem = editedItem.copy(isCritical = it) }
                    )
                    Text("IsCritical")
                }
                
                // Rotation Group field
                OutlinedTextField(
                    value = editedItem.rotationGroup.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { value ->
                            editedItem = editedItem.copy(rotationGroup = value)
                        }
                    },
                    label = { Text("RotationGroup") },
                                        modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Expected Answer dropdown
                ExposedDropdownMenuBox(
                    expanded = expectedAnswerExpanded,
                    onExpandedChange = { expectedAnswerExpanded = !expectedAnswerExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    OutlinedTextField(
                        value = editedItem.expectedAnswer.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("ExpectedAnswer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expectedAnswerExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expectedAnswerExpanded,
                        onDismissRequest = { expectedAnswerExpanded = false }
                    ) {
                        Answer.values().forEach { answer ->
                            DropdownMenuItem(
                                text = { Text(answer.name) },
                                onClick = {
                                    editedItem = editedItem.copy(expectedAnswer = answer)
                                    expectedAnswerExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    // ✅ VALIDATION: If allVehicleTypesEnabled is false, must have at least one vehicle type selected
                    if (!editedItem.allVehicleTypesEnabled && selectedVehicleTypeIds.isEmpty()) {
                        // Show error - cannot save without vehicle types when allVehicleTypesEnabled is false
                        validationError = "When 'All Vehicle Types' is disabled, you must select at least one vehicle type."
                        android.util.Log.e("QuestionValidation", "❌ Cannot save question: allVehicleTypesEnabled=false but no vehicle types selected")
                        return@TextButton
                    }
                    
                    // Clear any previous validation errors
                    validationError = null
                    
                    // Update the item with selected vehicle types before saving
                    val updatedItem = editedItem.copy(supportedVehicleTypeIds = selectedVehicleTypeIds)
                    android.util.Log.d("QuestionValidation", "✅ Saving question: allVehicleTypesEnabled=${editedItem.allVehicleTypesEnabled}, vehicleTypes=${selectedVehicleTypeIds.size}")
                    onSave(updatedItem)
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

    // Energy Source Selection Dialog
    if (energySourceExpanded) {
        AlertDialog(
            onDismissRequest = { energySourceExpanded = false },
            title = { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Energy Sources")
                    Text(
                        text = "${editedItem.energySourceEnum.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = app.forku.domain.model.vehicle.EnergySourceEnum.values().toList(),
                            key = { it.name }
                        ) { energySource ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (editedItem.energySourceEnum.contains(energySource)) {
                                            editedItem = editedItem.copy(
                                                energySourceEnum = editedItem.energySourceEnum - energySource
                                            )
                                        } else {
                                            editedItem = editedItem.copy(
                                                energySourceEnum = editedItem.energySourceEnum + energySource
                                            )
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = editedItem.energySourceEnum.contains(energySource),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            editedItem = editedItem.copy(
                                                energySourceEnum = editedItem.energySourceEnum + energySource
                                            )
                                        } else {
                                            editedItem = editedItem.copy(
                                                energySourceEnum = editedItem.energySourceEnum - energySource
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = energySource.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { energySourceExpanded = false }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        // Clear all energy sources
                        editedItem = editedItem.copy(energySourceEnum = emptyList())
                    }
                ) {
                    Text("Clear All")
                }
            }
        )
    }

    // Vehicle Type Selection Dialog
    if (vehicleTypeExpanded) {
        Log.d("VehicleTypeDialog", "🎭 [DIALOG-OPEN] selectedVehicleTypeIds: $selectedVehicleTypeIds (size: ${selectedVehicleTypeIds.size})")
        selectedVehicleTypeIds.forEach { id ->
            val name = availableVehicleTypes.find { it.Id == id }?.Name ?: "Unknown"
            Log.d("VehicleTypeDialog", "🎭 [DIALOG-OPEN] Currently selected: $id → $name")
        }
        AlertDialog(
            onDismissRequest = { vehicleTypeExpanded = false },
            title = { 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Vehicle Types")
                    Text(
                        text = "${selectedVehicleTypeIds.size} selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // Limitar altura máxima
                ) {
                    // Search/filter section (opcional)
                    if (availableVehicleTypes.size > 10) {
                        Text(
                            text = "Tap to select/deselect vehicle types:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Scrollable list
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = availableVehicleTypes,
                            key = { it.Id }
                        ) { vehicleType ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedVehicleTypeIds.contains(vehicleType.Id)) {
                                            selectedVehicleTypeIds = selectedVehicleTypeIds - vehicleType.Id
                                            Log.d("VehicleTypeDialog", "❌ [DESELECT] ${vehicleType.Name} (${vehicleType.Id})")
                                        } else {
                                            selectedVehicleTypeIds = selectedVehicleTypeIds + vehicleType.Id
                                            Log.d("VehicleTypeDialog", "✅ [SELECT] ${vehicleType.Name} (${vehicleType.Id})")
                                            // Clear validation error when adding vehicle types
                                            validationError = null
                                        }
                                        Log.d("VehicleTypeDialog", "🔄 [UPDATE] New selection: $selectedVehicleTypeIds (count: ${selectedVehicleTypeIds.size})")
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedVehicleTypeIds.contains(vehicleType.Id),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            selectedVehicleTypeIds = selectedVehicleTypeIds + vehicleType.Id
                                            Log.d("VehicleTypeDialog", "☑️ [CHECKBOX-ON] ${vehicleType.Name} (${vehicleType.Id})")
                                            // Clear validation error when adding vehicle types
                                            validationError = null
                                        } else {
                                            selectedVehicleTypeIds = selectedVehicleTypeIds - vehicleType.Id
                                            Log.d("VehicleTypeDialog", "☐ [CHECKBOX-OFF] ${vehicleType.Name} (${vehicleType.Id})")
                                        }
                                        Log.d("VehicleTypeDialog", "📋 [CHECKBOX-UPDATE] Current: $selectedVehicleTypeIds (count: ${selectedVehicleTypeIds.size})")
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = vehicleType.Name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        Log.d("VehicleTypeDialog", "🏁 [DONE] Final selected IDs: $selectedVehicleTypeIds (count: ${selectedVehicleTypeIds.size})")
                        selectedVehicleTypeIds.forEach { id ->
                            val name = availableVehicleTypes.find { it.Id == id }?.Name ?: "Unknown"
                            Log.d("VehicleTypeDialog", "🏁 [DONE] Final selection: $id → $name")
                        }
                        Log.d("VehicleTypeDialog", "🔒 [CLOSE] Persisting and closing dialog")
                        vehicleTypeExpanded = false 
                    }
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        // Clear all selections
                        selectedVehicleTypeIds = emptySet()
                    }
                ) {
                    Text("Clear All")
                }
            }
        )
    }
} 