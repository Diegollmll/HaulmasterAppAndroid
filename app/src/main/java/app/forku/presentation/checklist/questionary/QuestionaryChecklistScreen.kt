package app.forku.presentation.checklist.questionary

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import app.forku.data.model.QuestionaryChecklistDto
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.navigation.NavController
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
                    onDismiss = { viewModel.clearSelection() },
                    onSave = { questionary ->
                        if (questionary.id == null) {
                            viewModel.createQuestionary(questionary)
                        } else {
                            viewModel.updateQuestionary(questionary)
                        }
                        viewModel.clearSelection()
                    }
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
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        questionary.metadata?.let { metadata ->
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "v${metadata.version ?: "1.0"} • ${metadata.totalQuestions} questions",
                                style = MaterialTheme.typography.bodySmall
                            )
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
    onDismiss: () -> Unit,
    onSave: (QuestionaryChecklistDto) -> Unit
) {
    var title by remember { mutableStateOf(questionary.title) }
    var description by remember { mutableStateOf(questionary.description ?: "") }
    
    // Validation
    val isTitleValid = title.isNotBlank()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (questionary.id == null) "Create Questionary" else "Edit Questionary") },
        text = {
            Column {
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
                
                // Metadata Preview
                questionary.metadata?.let { metadata ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Questionary Settings",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Total Questions: ${metadata.totalQuestions}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "Questions Per Check: ${metadata.questionsPerCheck}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "Criticality Levels: ${metadata.criticalityLevels.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "Energy Sources: ${metadata.energySources.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Update the questionary with edited values
                    val updatedQuestionary = questionary.copy(
                        title = title,
                        description = description.ifBlank { null }
                    )
                    onSave(updatedQuestionary)
                },
                enabled = isTitleValid
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