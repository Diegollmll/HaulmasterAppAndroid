package app.forku.presentation.checklist.questionary

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.core.network.NetworkConnectivityManager

import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.navigation.NavController
import app.forku.data.api.dto.QuestionaryChecklistDto
import app.forku.data.api.dto.QuestionaryChecklistMetadataDto
import app.forku.data.api.dto.QuestionaryChecklistRotationRulesDto

import kotlinx.coroutines.delay

@Composable
fun QuestionaryChecklistScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: QuestionaryChecklistViewModel = hiltViewModel()
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
        Log.e("QuestionaryScreen", "Error state: ${uiState.error}")
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
        topBarTitle = "Questionary Checklists",
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
                            text = "Loaded ${uiState.questionaries.size} questionaries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Refresh button
                        IconButton(
                            onClick = { viewModel.loadQuestionaries() }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Add Questionary Button
                    Button(
                        onClick = { 
                            val newQuestionary = viewModel.createDefaultQuestionary(
                                title = "New Questionary",
                                description = "New questionary created on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}"
                            )
                            viewModel.selectQuestionary(newQuestionary)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Questionary Checklist")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Questionaries List
                    if (uiState.questionaries.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "No questionary checklists found. Create one to get started.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { 
                                        val testQuestionary = viewModel.createDefaultQuestionary(
                                            title = "Test Questionary ${(1000..9999).random()}",
                                            description = "This is a test questionary created on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date())}"
                                        )
                                        viewModel.createQuestionary(testQuestionary)
                                    }
                                ) {
                                    Text("Create Test Questionary")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = uiState.questionaries,
                                key = { it.id ?: "new-${it.hashCode()}" }
                            ) { questionary ->
                                QuestionaryItem(
                                    questionary = questionary,
                                    onEdit = { viewModel.selectQuestionary(questionary) },
                                    onDelete = { viewModel.deleteQuestionary(questionary) },
                                    onClick = {
                                        questionary.id?.let { questionaryId ->
                                            navController.navigate(Screen.QuestionaryItems.createRoute(questionaryId))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Add/Edit Dialog
            if (uiState.isEditMode) {
                QuestionaryFormDialog(
                    questionary = uiState.selectedQuestionary ?: viewModel.createDefaultQuestionary("New Questionary"),
                    uiState = uiState,
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { questionary ->
                        if (questionary.id == null) {
                            viewModel.createQuestionary(questionary)
                        } else {
                            viewModel.updateQuestionary(questionary)
                        }
                        viewModel.clearSelection()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun QuestionaryItem(
    questionary: QuestionaryChecklistDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
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
                        text = questionary.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    questionary.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Metadata information display
                    questionary.metadata?.let { metadata ->
                        // Item count with correct total questions
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
                                    text = "Items: ${metadata.totalQuestions}",
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
                                    text = "Max Per Check: ${questionary.rotationRules?.maxQuestionsPerCheck}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    if (questionary.id != null) {
                        Text(
                            text = "ID: ${questionary.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
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
            
            // Divider + Manage Questions button
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = {
                    if (questionary.id != null) {
                        onClick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = questionary.id != null
            ) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Questions")
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
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
                    Text("Are you sure you want to delete '${questionary.title}'?") 
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This will permanently remove this questionary checklist and all associated data.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (questionary.id != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Questionary ID: ${questionary.id}",
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
private fun QuestionaryFormDialog(
    questionary: QuestionaryChecklistDto,
    uiState: QuestionaryChecklistUiState,
    onDismiss: () -> Unit,
    onSave: (QuestionaryChecklistDto) -> Unit,
    viewModel: QuestionaryChecklistViewModel
) {
    var title by remember { mutableStateOf(questionary.title) }
    var description by remember { mutableStateOf(questionary.description ?: "") }
    var isDefault by remember { mutableStateOf(questionary.isDefault) }
    var businessId by remember { mutableStateOf(questionary.businessId ?: "") }
    var siteId by remember { mutableStateOf(questionary.siteId ?: "") }
    
    // Fields for metadata
    var version by remember { 
        mutableStateOf(questionary.metadata?.version ?: "1.0") 
    }
    var criticalityLevels by remember { 
        mutableStateOf(questionary.metadata?.criticalityLevels?.joinToString(", ") ?: "CRITICAL, STANDARD") 
    }
    var energySources by remember { 
        mutableStateOf(questionary.metadata?.energySources?.joinToString(", ") ?: "ALL, ELECTRIC, LPG, DIESEL") 
    }
    var vehicleTypes by remember { 
        mutableStateOf(questionary.metadata?.vehicleTypes?.joinToString(", ") ?: "ALL") 
    }
    var rotationGroups by remember {
        mutableStateOf(questionary.metadata?.rotationGroups?.toString() ?: "4")
    }

    // Fields for rotation rules
    var maxQuestionsPerCheck by remember {
        mutableStateOf(questionary.rotationRules?.maxQuestionsPerCheck?.toString() ?: "10")
    }
    var requiredCategories by remember {
        mutableStateOf(questionary.rotationRules?.requiredCategories?.joinToString(", ") 
            ?: "Visual Inspection, Mechanical, Safety Equipment")
    }
    var criticalQuestionMinimum by remember {
        mutableStateOf(questionary.rotationRules?.criticalQuestionMinimum?.toString() ?: "6")
    }
    var standardQuestionMaximum by remember {
        mutableStateOf(questionary.rotationRules?.standardQuestionMaximum?.toString() ?: "4")
    }
    
    // Business selector state
    var showBusinessDropdown by remember { mutableStateOf(false) }
    val selectedBusiness = businessId.let { id ->
        if (id.isNotEmpty()) uiState.businesses.find { it.id == id } else null
    }
    
    // Validation
    val isTitleValid = title.isNotBlank()
    val isRotationGroupsValid = rotationGroups.toIntOrNull() != null &&
                               rotationGroups.toIntOrNull()!! > 0
    val isMaxQuestionsValid = maxQuestionsPerCheck.toIntOrNull() != null &&
                             maxQuestionsPerCheck.toIntOrNull()!! > 0
    val isCriticalMinimumValid = criticalQuestionMinimum.toIntOrNull() != null &&
                                criticalQuestionMinimum.toIntOrNull()!! >= 0
    val isStandardMaximumValid = standardQuestionMaximum.toIntOrNull() != null &&
                                standardQuestionMaximum.toIntOrNull()!! >= 0
    
    val formScrollState = rememberScrollState()
    
    // Helper function for numbers-only input filters
    val onNumberInput: (String, (String) -> Unit) -> Unit = { input, setter ->
        if (input.all { it.isDigit() } || input.isEmpty()) {
            setter(input)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (questionary.id == null) "Create Questionary" else "Edit Questionary") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(formScrollState)
            ) {
                // 1. Basic Information Section
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isTitleValid,
                    supportingText = {
                        if (!isTitleValid) {
                            Text("Title cannot be empty")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 2. Additional Settings Section
                Text(
                    text = "Additional Settings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Is Default
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )
                    Text(
                        text = "Set as Default",
                        modifier = Modifier.clickable { isDefault = !isDefault }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Business Selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedBusiness?.name ?: businessId,
                        onValueChange = { },
                        label = { Text("Business") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showBusinessDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select Business")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = if (selectedBusiness != null) {
                            { Text("ID: ${selectedBusiness.id}") }
                        } else null
                    )
                    
                    DropdownMenu(
                        expanded = showBusinessDropdown,
                        onDismissRequest = { showBusinessDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                businessId = ""
                                siteId = "" // Clear site when business is cleared
                                viewModel.selectBusiness(null)
                                viewModel.selectSite(null)
                                showBusinessDropdown = false
                            }
                        )
                        uiState.businesses.forEach { business ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(business.name)
                                        Text(
                                            text = "ID: ${business.id}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    businessId = business.id
                                    viewModel.selectBusiness(business)
                                    showBusinessDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Site Selector
                var showSiteDropdown by remember { mutableStateOf(false) }
                val selectedSite = uiState.selectedSite ?: 
                    (if (siteId.isNotEmpty()) uiState.sites.find { it.id == siteId } else null)

                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSite?.name ?: siteId,
                        onValueChange = { },
                        label = { Text("Site") },
                        readOnly = true,
                        enabled = uiState.selectedBusiness != null,
                        trailingIcon = if (uiState.selectedBusiness != null) {
                            {
                                IconButton(onClick = { showSiteDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, "Select Site")
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = if (uiState.selectedBusiness == null) {
                            { Text("Select a business first") }
                        } else if (selectedSite != null) {
                            { Text("ID: ${selectedSite.id}") }
                        } else null
                    )
                    
                    DropdownMenu(
                        expanded = showSiteDropdown,
                        onDismissRequest = { showSiteDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                siteId = ""
                                viewModel.selectSite(null)
                                showSiteDropdown = false
                            }
                        )
                        uiState.sites.forEach { site ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(site.name)
                                        Text(
                                            text = site.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    siteId = site.id
                                    viewModel.selectSite(site)
                                    showSiteDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 3. Rotation Rules Section
                Text(
                    text = "Rotation Rules",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Categories Multi-select
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Required Categories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
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
                            if (uiState.categories.isEmpty()) {
                                Text(
                                    text = "No categories available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else {
                                uiState.categories.forEach { category ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = category.id?.let { it in uiState.selectedCategoryIds } ?: false,
                                            onCheckedChange = { 
                                                category.id?.let { viewModel.toggleCategorySelection(it) }
                                            }
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 8.dp)
                                        ) {
                                            Text(
                                                text = category.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            category.description?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Show selected categories count
                    Text(
                        text = "${uiState.selectedCategoryIds.size} categories selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = maxQuestionsPerCheck,
                    onValueChange = { onNumberInput(it) { maxQuestionsPerCheck = it } },
                    label = { Text("Max Questions Per Check") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isMaxQuestionsValid,
                    supportingText = {
                        if (!isMaxQuestionsValid) {
                            Text("Please enter a valid number greater than 0")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = criticalQuestionMinimum,
                    onValueChange = { onNumberInput(it) { criticalQuestionMinimum = it } },
                    label = { Text("Critical Question Minimum") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isCriticalMinimumValid,
                    supportingText = {
                        if (!isCriticalMinimumValid) {
                            Text("Please enter a valid number (0 or greater)")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = standardQuestionMaximum,
                    onValueChange = { onNumberInput(it) { standardQuestionMaximum = it } },
                    label = { Text("Standard Question Maximum") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isStandardMaximumValid,
                    supportingText = {
                        if (!isStandardMaximumValid) {
                            Text("Please enter a valid number (0 or greater)")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // 4. Metadata Settings Section
                Text(
                    text = "Metadata Settings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = version,
                    onValueChange = { version = it },
                    label = { Text("Version") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = rotationGroups,
                    onValueChange = { onNumberInput(it) { rotationGroups = it } },
                    label = { Text("Rotation Groups") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isRotationGroupsValid,
                    supportingText = {
                        if (!isRotationGroupsValid) {
                            Text("Please enter a valid number greater than 0")
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    enabled = false,
                    readOnly = false,
                    value = criticalityLevels,
                    onValueChange = { criticalityLevels = it },
                    label = { Text("Criticality Levels (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: CRITICAL, STANDARD") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Energy Sources Multi-select
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Energy Sources",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // State for energy sources selection
                    val energySourcesList = energySources.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    var selectedEnergySources by remember { mutableStateOf(energySourcesList.toSet()) }
                    val hasAllEnergySource = selectedEnergySources.contains("ALL")
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // ALL option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasAllEnergySource,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    // If ALL is selected, clear other selections and just add ALL
                                    selectedEnergySources = setOf("ALL")
                                    energySources = "ALL"
                                } else {
                                    // If ALL is unselected, remove it and keep other selections
                                    selectedEnergySources = selectedEnergySources - "ALL"
                                    energySources = selectedEnergySources.joinToString(", ")
                                }
                            }
                        )
                        Text(
                            text = "ALL",
                            modifier = Modifier
                                .clickable {
                                    val newState = !hasAllEnergySource
                                    if (newState) {
                                        selectedEnergySources = setOf("ALL")
                                        energySources = "ALL"
                                    } else {
                                        selectedEnergySources = selectedEnergySources - "ALL"
                                        energySources = selectedEnergySources.joinToString(", ")
                                    }
                                }
                                .padding(start = 8.dp)
                        )
                    }
                    
                    // Other energy sources from API
                    if (uiState.energySources.isNotEmpty()) {
                        uiState.energySources.forEach { source ->
                            if (source.id != null && source.name != "ALL") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedEnergySources.contains(source.id),
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                // If a specific source is selected, remove ALL
                                                val newSelection = (selectedEnergySources - "ALL") + source.id
                                                selectedEnergySources = newSelection
                                                energySources = newSelection.joinToString(", ")
                                            } else {
                                                // If a source is unselected, just remove it
                                                val newSelection = selectedEnergySources - source.id
                                                selectedEnergySources = newSelection
                                                energySources = newSelection.joinToString(", ")
                                            }
                                        },
                                        enabled = !hasAllEnergySource
                                    )
                                    Text(
                                        text = "${source.name} (${source.id})",
                                        modifier = Modifier
                                            .clickable(enabled = !hasAllEnergySource) {
                                                val newState = !selectedEnergySources.contains(source.id)
                                                if (newState) {
                                                    val newSelection = (selectedEnergySources - "ALL") + source.id
                                                    selectedEnergySources = newSelection
                                                    energySources = newSelection.joinToString(", ")
                                                } else {
                                                    val newSelection = selectedEnergySources - source.id
                                                    selectedEnergySources = newSelection
                                                    energySources = newSelection.joinToString(", ")
                                                }
                                            }
                                            .padding(start = 8.dp),
                                        color = if (hasAllEnergySource) 
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Display current selection
                    Text(
                        text = "Selected: $energySources",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                // Read-only text field for display
                OutlinedTextField(
                    value = energySources,
                    onValueChange = { /* Read only */ },
                    label = { Text("Energy Sources (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: ALL, [ID1], [ID2]") },
                    readOnly = true,
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Vehicle Types Multi-select
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Vehicle Types",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // State for vehicle types selection
                    val vehicleTypesList = vehicleTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    var selectedVehicleTypes by remember { mutableStateOf(vehicleTypesList.toSet()) }
                    val hasAllVehicleType = selectedVehicleTypes.contains("ALL")
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // ALL option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasAllVehicleType,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    // If ALL is selected, clear other selections and just add ALL
                                    selectedVehicleTypes = setOf("ALL")
                                    vehicleTypes = "ALL"
                                } else {
                                    // If ALL is unselected, remove it and keep other selections
                                    selectedVehicleTypes = selectedVehicleTypes - "ALL"
                                    vehicleTypes = selectedVehicleTypes.joinToString(", ")
                                }
                            }
                        )
                        Text(
                            text = "ALL",
                            modifier = Modifier
                                .clickable {
                                    val newState = !hasAllVehicleType
                                    if (newState) {
                                        selectedVehicleTypes = setOf("ALL")
                                        vehicleTypes = "ALL"
                                    } else {
                                        selectedVehicleTypes = selectedVehicleTypes - "ALL"
                                        vehicleTypes = selectedVehicleTypes.joinToString(", ")
                                    }
                                }
                                .padding(start = 8.dp)
                        )
                    }
                    
                    // Available vehicle types
                    uiState.vehicleTypes.forEach { vehicleType ->
                        val typeId = vehicleType.id
                        val typeName = vehicleType.name
                        if (typeId != null && typeName != "ALL") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedVehicleTypes.contains(typeId),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            // If a specific type is selected, remove ALL
                                            val newSelection = (selectedVehicleTypes - "ALL") + typeId
                                            selectedVehicleTypes = newSelection
                                            vehicleTypes = newSelection.joinToString(", ")
                                        } else {
                                            // If a type is unselected, just remove it
                                            val newSelection = selectedVehicleTypes - typeId
                                            selectedVehicleTypes = newSelection
                                            vehicleTypes = newSelection.joinToString(", ")
                                        }
                                    },
                                    enabled = !hasAllVehicleType
                                )
                                Text(
                                    text = "$typeName ($typeId)",
                                    modifier = Modifier
                                        .clickable(enabled = !hasAllVehicleType) {
                                            val newState = !selectedVehicleTypes.contains(typeId)
                                            if (newState) {
                                                val newSelection = (selectedVehicleTypes - "ALL") + typeId
                                                selectedVehicleTypes = newSelection
                                                vehicleTypes = newSelection.joinToString(", ")
                                            } else {
                                                val newSelection = selectedVehicleTypes - typeId
                                                selectedVehicleTypes = newSelection
                                                vehicleTypes = newSelection.joinToString(", ")
                                            }
                                        }
                                        .padding(start = 8.dp),
                                    color = if (hasAllVehicleType) 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Display current selection
                    Text(
                        text = "Selected: $vehicleTypes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                // Read-only text field for display
                OutlinedTextField(
                    value = vehicleTypes,
                    onValueChange = { /* Read only */ },
                    label = { Text("Vehicle Types (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Example: ALL, [ID1], [ID2]") },
                    readOnly = true,
                    enabled = false
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show current metadata
                questionary.metadata?.let { metadata ->
                    val totalQuestionsText = if (questionary.items.isNotEmpty()) {
                        "Total Questions: ${questionary.items.size}"
                    } else {
                        "Total Questions: ${metadata.totalQuestions}"
                    }
                    
                    Text(
                        text = totalQuestionsText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Parse the comma-separated values
                    val criticalityList = criticalityLevels.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val energySourcesList = energySources.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val vehicleTypesList = vehicleTypes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val requiredCategoriesList = requiredCategories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    val rotationGroupsValue = rotationGroups.toIntOrNull() ?: 4
                    val maxQuestionsValue = maxQuestionsPerCheck.toIntOrNull() ?: 10
                    val criticalMinimumValue = criticalQuestionMinimum.toIntOrNull() ?: 6
                    val standardMaximumValue = standardQuestionMaximum.toIntOrNull() ?: 4
                    
                    // Get current total of questions or use existing value
                    val totalQuestions = if (questionary.items.isNotEmpty()) {
                        questionary.items.size
                    } else {
                        questionary.metadata?.totalQuestions ?: 20
                    }
                    
                    // Update the questionary with edited values and metadata
                    val updatedMetadata = (questionary.metadata ?: QuestionaryChecklistMetadataDto()).copy(
                        version = version,
                        totalQuestions = totalQuestions,
                        rotationGroups = rotationGroupsValue,
                        criticalityLevels = criticalityList,
                        energySources = energySourcesList,
                        vehicleTypes = vehicleTypesList
                    )
                    
                    val updatedRotationRules = (questionary.rotationRules ?: QuestionaryChecklistRotationRulesDto()).copy(
                        maxQuestionsPerCheck = maxQuestionsValue,
                        requiredCategories = uiState.selectedCategoryIds,
                        criticalQuestionMinimum = criticalMinimumValue,
                        standardQuestionMaximum = standardMaximumValue
                    )

                    // Get the business and site IDs from the UI state, using empty string if null
                    val selectedBusinessId = uiState.selectedBusiness?.id ?: ""
                    val selectedSiteId = uiState.selectedSite?.id ?: ""
                    
                    Log.d("QuestionaryForm", "Saving with Business ID: '$selectedBusinessId', Site ID: '$selectedSiteId'")
                    
                    val updatedQuestionary = questionary.copy(
                        title = title,
                        description = description.ifBlank { null },
                        isDefault = isDefault,
                        businessId = selectedBusinessId,  // Will be empty string when no business selected
                        siteId = selectedSiteId,         // Will be empty string when no site selected
                        metadata = updatedMetadata,
                        rotationRules = updatedRotationRules
                    )
                    
                    onSave(updatedQuestionary)
                },
                enabled = isTitleValid && isRotationGroupsValid && isMaxQuestionsValid &&
                         isCriticalMinimumValid && isStandardMaximumValid
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
fun QuestionarySelectionScreen(
    navController: NavController,
    viewModel: QuestionaryChecklistViewModel = hiltViewModel()
) {
    val questionaries by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    var selectedQuestionary by remember { mutableStateOf<QuestionaryChecklistDto?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text("Select a Questionary Checklist", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dropdown for selecting a questionary
            Box {
                Text(
                    text = selectedQuestionary?.title ?: "Select Questionary",
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surface)
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    questionaries.questionaries.forEach { questionary ->
                        DropdownMenuItem(
                            text = { Text(text = questionary.title ?: "No Title") },
                            onClick = {
                                selectedQuestionary = questionary
                                expanded = false
                                questionary.id?.let { id ->
                                    navController.navigate(Screen.QuestionaryItems.createRoute(id))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
} 