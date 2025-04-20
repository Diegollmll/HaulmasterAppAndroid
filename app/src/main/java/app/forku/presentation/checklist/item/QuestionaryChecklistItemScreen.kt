package app.forku.presentation.checklist.item

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.core.network.NetworkConnectivityManager
import app.forku.data.api.dto.QuestionaryChecklistItemDto
import app.forku.data.api.dto.QuestionaryChecklistItemCategoryDto
import app.forku.data.api.dto.QuestionaryChecklistItemSubcategoryDto
import app.forku.presentation.common.components.BaseScreen
import androidx.navigation.NavController
import app.forku.data.api.dto.vehicle.VehicleComponentDto
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
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subcategories by viewModel.subcategories.collectAsStateWithLifecycle()
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
                                
//                                Spacer(modifier = Modifier.height(16.dp))
//
//                                Button(
//                                    onClick = {
//                                        val testItem = viewModel.createEmptyItem().copy(
//                                            question = "Test Question ${(1000..9999).random()}",
//                                            description = "This is a test question created on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}"
//                                        )
//                                        viewModel.createItem(testItem)
//                                    }
//                                ) {
//                                    Text("Create Test Item")
//                                }
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
                    onSave = { item: QuestionaryChecklistItemDto ->
                        if (item.id == null) {
                            viewModel.createItem(item)
                        } else {
                            viewModel.updateItem(item)
                        }
                        viewModel.clearSelection()
                    },
                    categories = categories,
                    subcategories = subcategories,
                    viewModel = viewModel
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
    onSave: (QuestionaryChecklistItemDto) -> Unit,
    categories: List<QuestionaryChecklistItemCategoryDto>,
    subcategories: List<QuestionaryChecklistItemSubcategoryDto>,
    viewModel: QuestionaryChecklistItemViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var question by remember { mutableStateOf(item.question) }
    var description by remember { mutableStateOf(item.description ?: "") }
    var isCritical by remember { mutableStateOf(item.isCritical) }
    var rotationGroup by remember { mutableStateOf(item.rotationGroup.toString()) }
    var position by remember { mutableStateOf(item.position.toString()) }
    var isActive by remember { mutableStateOf(item.isActive) }
    var component by remember { mutableStateOf(item.componentId ?: "") }
    
    // New fields
    var expectedAnswer by remember { mutableStateOf(item.expectedAnswer ?: "") }
    var isRandomQuestion by remember { mutableStateOf(item.isRandomQuestion ?: false) }
    var checksUntilAppear by remember { mutableStateOf(item.checksUntilAppear?.toString() ?: "0") }
    var maxChecksUntilAppear by remember { mutableStateOf(item.maxChecksUntilAppear?.toString() ?: "0") }
    
    // Expected answer dropdown state
    var expandedAnswerDropdown by remember { mutableStateOf(false) }
    val answerOptions = listOf("PASS", "FAIL")
    
    // Energy Source and Vehicle Type selections
    var selectedEnergySources by remember { mutableStateOf(item.energySource.toSet()) }
    var selectedVehicleTypes by remember { mutableStateOf(item.vehicleType.toSet()) }
    
    // New fields for category and subcategory
    var selectedCategory by remember { mutableStateOf(item.categoryId ?: "") }
    var selectedSubcategory by remember { mutableStateOf(item.subCategoryId ?: "") }
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedSubcategory by remember { mutableStateOf(false) }
    
    // Validation
    val isQuestionValid = question.isNotBlank()
    val isRotationGroupValid = rotationGroup.toIntOrNull() != null
    val isPositionValid = position.toIntOrNull() != null
    val isChecksUntilAppearValid = checksUntilAppear.toIntOrNull() != null && checksUntilAppear.toIntOrNull()!! >= 0
    val isMaxChecksUntilAppearValid = maxChecksUntilAppear.toIntOrNull() != null && maxChecksUntilAppear.toIntOrNull()!! >= 0

    // Update form validation
    val isFormValid = isQuestionValid && isRotationGroupValid && isPositionValid && 
                     isChecksUntilAppearValid && isMaxChecksUntilAppearValid

    // Helper function for numbers-only input filters with explicit types
    fun handleNumericInput(value: String, onUpdate: (String) -> Unit) {
        if (value.all { it.isDigit() } || value.isEmpty()) {
            onUpdate(value)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item.id == null) "Create Question Item" else "Edit Question Item") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Question information
                Text(
                    text = "Question information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                
                // Category Dropdown
                Text("Category")
                Box {
                    Text(
                        text = categories.find { it.id == selectedCategory }?.name ?: "Select Category",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = true }
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category.id ?: ""
                                    expandedCategory = false
                                    viewModel.selectCategory(selectedCategory)
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subcategory Dropdown
                Text("Subcategory")
                Box {
                    Text(
                        text = subcategories.find { it.id == selectedSubcategory }?.name ?: "Select Subcategory",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSubcategory = true }
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    DropdownMenu(
                        expanded = expandedSubcategory,
                        onDismissRequest = { expandedSubcategory = false }
                    ) {
                        subcategories.filter { it.categoryId == selectedCategory }.forEach { subcategory ->
                            DropdownMenuItem(
                                text = { Text(subcategory.name) },
                                onClick = {
                                    selectedSubcategory = subcategory.id ?: ""
                                    expandedSubcategory = false
                                }
                            )
                        }
                    }
                }
                
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

                // Component Dropdown
                Text(
                    text = "Vehicle Component",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (uiState.availableComponents.isEmpty()) {
                            Text(
                                text = "Loading components...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            var expanded by remember { mutableStateOf(false) }
                            val selectedComponent = uiState.availableComponents.find { it.id == component }

                            OutlinedTextField(
                                value = selectedComponent?.name ?: "Select Component",
                                onValueChange = { },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Select Component")
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                ComponentDropdownContent(
                                    components = uiState.availableComponents,
                                    onSelectComponent = { component = it },
                                    onDismiss = { expanded = false }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                // Component
                OutlinedTextField(
                    enabled = false,
                    readOnly = true,
                    value = component,
                    onValueChange = { component = it },
                    label = { Text("Component") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Energy Sources Multi-select
                Text(
                    text = "Energy Sources",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (uiState.availableEnergySources.isEmpty()) {
                            Text(
                                text = "Loading energy sources...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            // First, show the ALL option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = "ALL" in selectedEnergySources,
                                    onCheckedChange = { checked ->
                                        selectedEnergySources = if (checked) {
                                            setOf("ALL")
                                        } else {
                                            emptySet()
                                        }
                                    }
                                )
                                Text(
                                    text = "ALL",
                                    modifier = Modifier
                                        .clickable {
                                            selectedEnergySources = if ("ALL" in selectedEnergySources) {
                                                emptySet()
                                            } else {
                                                setOf("ALL")
                                            }
                                        }
                                        .padding(start = 8.dp)
                                )
                            }

                            // Then show other options
                            uiState.availableEnergySources.filter { it != "ALL" }.forEach { source ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = source in selectedEnergySources,
                                        onCheckedChange = { checked ->
                                            selectedEnergySources = if (checked) {
                                                if ("ALL" in selectedEnergySources) {
                                                    setOf(source)
                                                } else {
                                                    selectedEnergySources + source
                                                }
                                            } else {
                                                selectedEnergySources - source
                                            }
                                        },
                                        enabled = "ALL" !in selectedEnergySources
                                    )
                                    Text(
                                        text = source,
                                        modifier = Modifier
                                            .clickable(
                                                enabled = "ALL" !in selectedEnergySources
                                            ) {
                                                selectedEnergySources = if (source in selectedEnergySources) {
                                                    selectedEnergySources - source
                                                } else {
                                                    if ("ALL" in selectedEnergySources) {
                                                        setOf(source)
                                                    } else {
                                                        selectedEnergySources + source
                                                    }
                                                }
                                            }
                                            .padding(start = 8.dp),
                                        color = if ("ALL" in selectedEnergySources) 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Vehicle Types Multi-select
                Text(
                    text = "Vehicle Types",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        if (uiState.availableVehicleTypes.isEmpty()) {
                            Text(
                                text = "Loading vehicle types...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(8.dp)
                            )
                        } else {
                            // First, show the ALL option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = "ALL" in selectedVehicleTypes,
                                    onCheckedChange = { checked ->
                                        selectedVehicleTypes = if (checked) {
                                            setOf("ALL")
                                        } else {
                                            emptySet()
                                        }
                                    }
                                )
                                Text(
                                    text = "ALL",
                                    modifier = Modifier
                                        .clickable {
                                            selectedVehicleTypes = if ("ALL" in selectedVehicleTypes) {
                                                emptySet()
                                            } else {
                                                setOf("ALL")
                                            }
                                        }
                                        .padding(start = 8.dp)
                                )
                            }

                            // Then show other vehicle types
                            uiState.availableVehicleTypes.map { it.name }.filter { it != "ALL" }.forEach { type ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = type in selectedVehicleTypes,
                                        onCheckedChange = { checked ->
                                            selectedVehicleTypes = if (checked) {
                                                if ("ALL" in selectedVehicleTypes) {
                                                    setOf(type)
                                                } else {
                                                    selectedVehicleTypes + type
                                                }
                                            } else {
                                                selectedVehicleTypes - type
                                            }
                                        },
                                        enabled = "ALL" !in selectedVehicleTypes
                                    )
                                    Text(
                                        text = type,
                                        modifier = Modifier
                                            .clickable(
                                                enabled = "ALL" !in selectedVehicleTypes
                                            ) {
                                                selectedVehicleTypes = if (type in selectedVehicleTypes) {
                                                    selectedVehicleTypes - type
                                                } else {
                                                    if ("ALL" in selectedVehicleTypes) {
                                                        setOf(type)
                                                    } else {
                                                        selectedVehicleTypes + type
                                                    }
                                                }
                                            }
                                            .padding(start = 8.dp),
                                        color = if ("ALL" in selectedVehicleTypes) 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                // Rotation Group
                OutlinedTextField(
                    value = rotationGroup,
                    onValueChange = { handleNumericInput(it) { newValue -> rotationGroup = newValue } },
                    label = { Text("Rotation Group") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isRotationGroupValid,
                    supportingText = {
                        if (!isRotationGroupValid) {
                            Text("Please enter a valid number")
                        }
                    }
                )

                /////////////////////////////////////
                Spacer(modifier = Modifier.height(8.dp))

                // Position
                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = position,
                    onValueChange = { handleNumericInput(it) { newValue -> position = newValue } },
                    label = { Text("Position") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isPositionValid,
                    supportingText = {
                        if (!isPositionValid) {
                            Text("Please enter a valid number")
                        }
                    }
                )

                /////////////////////////////

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

                Spacer(modifier = Modifier.height(16.dp))

                // Expected Answer with Dropdown
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Expected Answer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(0.4f)) {
                            Button(
                                onClick = { expandedAnswerDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Select")
                            }

                            DropdownMenu(
                                expanded = expandedAnswerDropdown,
                                onDismissRequest = { expandedAnswerDropdown = false }
                            ) {
                                answerOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            expectedAnswer = option
                                            expandedAnswerDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            enabled = false,
                            readOnly = true,
                            value = expectedAnswer,
                            onValueChange = { expectedAnswer = it },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true
                        )


                    }

                    Spacer(modifier = Modifier.height(4.dp))

                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Question Occurrence",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Random Question Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isRandomQuestion,
                            onCheckedChange = { isRandomQuestion = it }
                        )
                        Text(
                            text = "Random Question",
                            modifier = Modifier.clickable { isRandomQuestion = !isRandomQuestion }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Max Checks Until Appear
                    OutlinedTextField(
                        value = maxChecksUntilAppear,
                        onValueChange = { handleNumericInput(it) { newValue -> maxChecksUntilAppear = newValue } },
                        label = { Text("Max Checks Until Appear") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isMaxChecksUntilAppearValid,
                        enabled = !isRandomQuestion,
                        readOnly = isRandomQuestion,
                        supportingText = {
                            if (!isMaxChecksUntilAppearValid) {
                                Text("Please enter a valid number (0 or greater)")
                            } else if (isRandomQuestion) {
                                Text("Disabled for random questions", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checks Until Appear
                    OutlinedTextField(
                        enabled = false,
                        readOnly = true,
                        value = checksUntilAppear,
                        onValueChange = { handleNumericInput(it) { newValue -> checksUntilAppear = newValue } },
                        label = { Text("Checks Until Appear") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isChecksUntilAppearValid,
                        supportingText = {
                            if (!isChecksUntilAppearValid) {
                                Text("Please enter a valid number (0 or greater)")
                            }
                        }
                    )

                }





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
                        energySource = selectedEnergySources.toList(),
                        vehicleType = selectedVehicleTypes.toList(),
                        componentId = component.ifBlank { null },
                        categoryId = selectedCategory.ifBlank { null },
                        subCategoryId = selectedSubcategory.ifBlank { null },
                        expectedAnswer = expectedAnswer.ifBlank { null },
                        isRandomQuestion = isRandomQuestion,
                        checksUntilAppear = checksUntilAppear.toIntOrNull() ?: 0,
                        maxChecksUntilAppear = maxChecksUntilAppear.toIntOrNull() ?: 0
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

@Composable
private fun ComponentDropdownContent(
    components: List<VehicleComponentDto>,
    onSelectComponent: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // None option
    DropdownMenuItem(
        text = { Text("None") },
        onClick = {
            onSelectComponent("")
            onDismiss()
        }
    )
    
    // Available components
    components.forEach { comp ->
        DropdownMenuItem(
            text = {
                Column {
                    Text(text = comp.name)
                    if (!comp.description.isNullOrBlank()) {
                        Text(
                            text = comp.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            onClick = {
                onSelectComponent(comp.id ?: "")
                onDismiss()
            }
        )
    }
} 