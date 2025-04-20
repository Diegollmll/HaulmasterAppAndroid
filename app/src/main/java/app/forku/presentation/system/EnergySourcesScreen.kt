package app.forku.presentation.system

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.data.api.dto.EnergySourceDto
import app.forku.presentation.common.components.BaseScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EnergySourcesScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: EnergySourceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEnergySource by remember { mutableStateOf<EnergySourceDto?>(null) }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Energy Sources",
        networkManager = networkManager
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Add Energy Source Button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Add Energy Source",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // List of Energy Sources
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.energySources) { energySource ->
                        EnergySourceItem(
                            energySource = energySource,
                            onEdit = { selectedEnergySource = energySource },
                            onDelete = { 
                                energySource.id?.let { id -> 
                                    viewModel.deleteEnergySource(id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Error handling
        state.error?.let { error ->
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
        if (showAddDialog || selectedEnergySource != null) {
            EnergySourceDialog(
                energySource = selectedEnergySource,
                onDismiss = {
                    showAddDialog = false
                    selectedEnergySource = null
                },
                onConfirm = { name ->
                    if (selectedEnergySource != null) {
                        selectedEnergySource?.id?.let { id ->
                            viewModel.updateEnergySource(id, name)
                        }
                        selectedEnergySource = null
                    } else {
                        viewModel.createEnergySource(name)
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun EnergySourceDialog(
    energySource: EnergySourceDto? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(energySource?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (energySource == null) "Add Energy Source" else "Edit Energy Source")
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (energySource == null) "Add" else "Update")
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
private fun EnergySourceItem(
    energySource: EnergySourceDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                text = energySource.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }
} 