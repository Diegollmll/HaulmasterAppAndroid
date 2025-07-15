package app.forku.presentation.checklist.manage_checklist

import android.util.Log
import android.widget.Toast
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
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.navigation.NavController
import app.forku.domain.model.checklist.Checklist
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext

@Composable
fun ManageChecklistScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: ManageChecklistViewModel = hiltViewModel()
) {
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
        Log.e("ManageChecklistScreen", "Error state: ${uiState.error}")
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
        topBarTitle = "Manage Checklists",
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
                    // Business and Site Filters
                    BusinessSiteFilters(
                        modifier = Modifier.padding(bottom = 16.dp),
                        onBusinessChanged = { businessId ->
                            viewModel.updateBusinessContext(businessId)
                        },
                        onSiteChanged = { siteId ->
                            viewModel.updateSiteContext(siteId)
                        },
                        showBusinessFilter = false, // ✅ Hide business filter
                        isCollapsible = true, // ✅ Make filters collapsible
                        initiallyExpanded = false, // ✅ Start collapsed
                        title = "Filter Checklists by Context",
                        businessContextManager = viewModel.businessContextManager
                    )
                    
                    // Debug text and refresh button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Debug text
                        Text(
                            text = "Loaded ${uiState.checklists.size} checklists",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.loadChecklists() }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Add Checklist Button
                    Button(
                        onClick = { 
                            viewModel.selectNewChecklist(
                                title = "New Checklist",
                                description = "New checklist created on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Checklist")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checklists List
                    if (uiState.checklists.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No checklists found. Create one to get started.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { 
                                        viewModel.createNewTestChecklist(
                                            title = "Test Checklist ${(1000..9999).random()}",
                                            description = "This is a test checklist created on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}"
                                        )
                                    }
                                ) {
                                    Text("Create Test Checklist")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = uiState.checklists,
                                key = { it.id }
                            ) { checklist ->
                                ChecklistItem(
                                    checklist = checklist,
                                    isEditable = viewModel.isChecklistEditable(checklist),
                                    onEdit = { viewModel.selectChecklist(checklist) },
                                    onDelete = { viewModel.deleteChecklist(checklist) },
                                    onClick = {
                                        val isEditable = viewModel.isChecklistEditable(checklist)
                                        navController.navigate(Screen.QuestionaryItems.createRoute(checklist.id, isEditable))
                                    },
                                    createdByUserName = viewModel.getUserName(checklist.goUserId) // ✅ New: Pass user name
                                )
                            }
                        }
                    }
                }
            }

            // Add/Edit Dialog
            if (uiState.isEditMode) {
                ChecklistFormDialog(
                    checklist = uiState.selectedChecklist ?: viewModel.createDefaultChecklist("New Checklist"),
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { checklist ->
                        if (checklist.id.isEmpty()) {
                            viewModel.createChecklist(checklist)
                        } else {
                            viewModel.updateChecklist(checklist)
                        }
                        viewModel.clearSelection()
                    },
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun ChecklistItem(
    checklist: Checklist,
    isEditable: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    createdByUserName: String? = null // ✅ New: User name who created the checklist
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = checklist.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Show creator information
                    if (createdByUserName != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Created by",
                                modifier = Modifier.size(14.dp),
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
                    
                    if (checklist.description.isNotEmpty()) {
                        Text(
                            text = checklist.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Metadata information display
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                text = "Items: ${checklist.items.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Icon(
                                    Icons.Default.CheckBox,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                text = "Max Per Check: ${checklist.maxQuestionsPerCheck}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                        }
                    }
                    
                    // ID hidden for better UX - only used internally
                    // if (checklist.id.isNotEmpty()) {
                    //     Text(
                    //         text = "ID: ${checklist.id}",
                    //         style = MaterialTheme.typography.bodySmall,
                    //         color = MaterialTheme.colorScheme.outline
                    //     )
                    // }
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
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                } else {
                    // Show read-only indicator for default checklists
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(8.dp)
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
                                text = "Default",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
            
                    Spacer(modifier = Modifier.height(8.dp))
            
            // Click to view items button
            Surface(
                onClick = onClick,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Items",
                        style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    if (showDeleteConfirmation && isEditable) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Checklist") },
            text = { Text("Are you sure you want to delete '${checklist.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChecklistFormDialog(
    checklist: Checklist,
    onDismiss: () -> Unit,
    onSave: (Checklist) -> Unit,
    uiState: ManageChecklistUiState
) {
    var title by remember { mutableStateOf(checklist.title) }
    var description by remember { mutableStateOf(checklist.description) }
    var maxQuestionsPerCheck by remember { mutableStateOf(checklist.maxQuestionsPerCheck.toString()) }
    var criticalQuestionMinimum by remember { mutableStateOf(checklist.criticalQuestionMinimum.toString()) }
    var standardQuestionMaximum by remember { mutableStateOf(checklist.standardQuestionMaximum.toString()) }
    var rotationGroups by remember { mutableStateOf(checklist.rotationGroups.toString()) }
    var allVehicleTypesEnabled by remember { mutableStateOf(checklist.allVehicleTypesEnabled) }
    var selectedCriticalityLevels by remember { mutableStateOf(checklist.criticalityLevels.toSet()) }
    var selectedEnergySources by remember { mutableStateOf(checklist.energySources.toSet()) }
    var selectedVehicleTypeIds by remember { mutableStateOf(checklist.supportedVehicleTypeIds) }
    var selectedCategoryIds by remember { mutableStateOf(checklist.requiredCategoryIds) }

    // ✅ Sync with pre-selected categories from backend when editing existing checklist
    LaunchedEffect(uiState.selectedCategoryIds, checklist.id) {
        if (checklist.id.isNotEmpty() && uiState.selectedCategoryIds.isNotEmpty()) {
            Log.d("ManageChecklistScreen", "Syncing selected categories from backend: ${uiState.selectedCategoryIds}")
            selectedCategoryIds = uiState.selectedCategoryIds.toSet()
        }
    }
    
    val isFormValid = title.isNotBlank() && 
                     maxQuestionsPerCheck.toIntOrNull() != null &&
                     criticalQuestionMinimum.toIntOrNull() != null &&
                     standardQuestionMaximum.toIntOrNull() != null &&
                     rotationGroups.toIntOrNull() != null &&
                     // ✅ VALIDATION: If allVehicleTypesEnabled is false, must have at least one vehicle type selected
                     (allVehicleTypesEnabled || selectedVehicleTypeIds.isNotEmpty())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (checklist.id.isEmpty()) "Create Checklist" else "Edit Checklist") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Checklist Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // IsDefault Field (Disabled/Read-only)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Checklist", style = MaterialTheme.typography.bodyMedium)
                        Text("Always set to false to avoid conflicts with global default", 
                             style = MaterialTheme.typography.bodySmall, 
                             color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = false, 
                        onCheckedChange = { }, // Disabled
                        enabled = false
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // All Vehicle Types Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable for All Vehicle Types", style = MaterialTheme.typography.bodyMedium)
                        Text("If enabled, this checklist applies to all vehicle types", 
                             style = MaterialTheme.typography.bodySmall, 
                             color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = allVehicleTypesEnabled, onCheckedChange = { allVehicleTypesEnabled = it })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Criticality Levels
                Text("Select Required Criticality Levels:", 
                     style = MaterialTheme.typography.titleSmall,
                     color = MaterialTheme.colorScheme.primary)
                Text("Choose which criticality levels this checklist should include",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    uiState.availableCriticalityLevels.forEach { level ->
                        val label = if (level == 0) "Critical" else "Standard"
                        FilterChip(
                            selected = selectedCriticalityLevels.contains(level),
                            onClick = {
                                selectedCriticalityLevels = if (selectedCriticalityLevels.contains(level))
                                    selectedCriticalityLevels - level else selectedCriticalityLevels + level
                            },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Energy Sources
                Text("Select Supported Energy Sources:", 
                     style = MaterialTheme.typography.titleSmall,
                     color = MaterialTheme.colorScheme.primary)
                Text("Choose which energy sources this checklist supports",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Debug: Log available energy sources
                LaunchedEffect(uiState.availableEnergySources) {
                    Log.d("ManageChecklistScreen", "Rendering Energy Sources:")
                    uiState.availableEnergySources.forEach { source ->
                        Log.d("ManageChecklistScreen", "- ${source.name} (apiValue: ${source.apiValue})")
                    }
                }
                
                FlowRow {
                    uiState.availableEnergySources.forEach { source ->
                        FilterChip(
                            selected = selectedEnergySources.contains(source.apiValue),
                            onClick = {
                                selectedEnergySources = if (selectedEnergySources.contains(source.apiValue))
                                    selectedEnergySources - source.apiValue else selectedEnergySources + source.apiValue
                            },
                            label = { Text(source.name) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Vehicle Types (only if not all enabled)
                if (!allVehicleTypesEnabled) {
                    val hasVehicleTypeError = !allVehicleTypesEnabled && selectedVehicleTypeIds.isEmpty()
                    Text("Select Supported Vehicle Types:", 
                         style = MaterialTheme.typography.titleSmall,
                         color = if (hasVehicleTypeError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    Text("Choose which specific vehicle types this checklist supports",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.outline)
                    
                    // ✅ VALIDATION ERROR MESSAGE
                    if (hasVehicleTypeError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "At least one vehicle type must be selected when 'All Vehicle Types' is disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow {
                        uiState.availableVehicleTypes.forEach { type ->
                            FilterChip(
                                selected = selectedVehicleTypeIds.contains(type.Id),
                                onClick = {
                                    selectedVehicleTypeIds = if (selectedVehicleTypeIds.contains(type.Id))
                                        selectedVehicleTypeIds - type.Id else selectedVehicleTypeIds + type.Id
                                },
                                label = { Text(type.Name) },
                                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Categories
                Text("Select Required Question Categories:", 
                     style = MaterialTheme.typography.titleSmall,
                     color = MaterialTheme.colorScheme.primary)
                Text("Choose which question categories must be included",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow {
                    uiState.availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryIds.contains(cat.id),
                            onClick = {
                                selectedCategoryIds = if (selectedCategoryIds.contains(cat.id))
                                    selectedCategoryIds - cat.id else selectedCategoryIds + cat.id
                            },
                            label = { Text(cat.name) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Numeric fields
                Text("Rotation and Question Settings:", 
                     style = MaterialTheme.typography.titleSmall,
                     color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                    OutlinedTextField(
                    value = maxQuestionsPerCheck,
                    onValueChange = { maxQuestionsPerCheck = it },
                    label = { Text("Max Questions Per Check") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = maxQuestionsPerCheck.toIntOrNull() == null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = criticalQuestionMinimum,
                    onValueChange = { criticalQuestionMinimum = it },
                    label = { Text("Critical Question Minimum") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = criticalQuestionMinimum.toIntOrNull() == null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = standardQuestionMaximum,
                    onValueChange = { standardQuestionMaximum = it },
                    label = { Text("Standard Question Maximum") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = standardQuestionMaximum.toIntOrNull() == null
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = rotationGroups,
                    onValueChange = { rotationGroups = it },
                    label = { Text("Rotation Groups") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = rotationGroups.toIntOrNull() == null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedChecklist = checklist.copy(
                        title = title,
                        description = description,
                        isDefault = false, // Always false as requested
                        allVehicleTypesEnabled = allVehicleTypesEnabled,
                        criticalityLevels = selectedCriticalityLevels.toList(),
                        energySources = selectedEnergySources.toList(),
                        supportedVehicleTypeIds = if (allVehicleTypesEnabled) emptySet() else selectedVehicleTypeIds,
                        requiredCategoryIds = selectedCategoryIds,
                        maxQuestionsPerCheck = maxQuestionsPerCheck.toIntOrNull() ?: 10,
                        criticalQuestionMinimum = criticalQuestionMinimum.toIntOrNull() ?: 5,
                        standardQuestionMaximum = standardQuestionMaximum.toIntOrNull() ?: 5,
                        rotationGroups = rotationGroups.toIntOrNull() ?: 4
                    )
                    onSave(updatedChecklist)
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