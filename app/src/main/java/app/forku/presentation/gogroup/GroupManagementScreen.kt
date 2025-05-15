package app.forku.presentation.gogroup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.domain.model.gogroup.GOGroup
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorBanner
import app.forku.presentation.common.components.BaseScreen
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.network.NetworkConnectivityManager
import androidx.navigation.NavController

@Composable
fun GroupManagementScreen(
    viewModel: GroupManagementViewModel = hiltViewModel(),
    onNavigateToRoles: (String) -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Group Management",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Group Management",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (state.error != null) {
                    ErrorBanner(
                        error = state.error!!,
                        onDismiss = viewModel::clearError
                    )
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.groups) { group ->
                        GroupCard(
                            group = group,
                            onUpdateGroup = { description, isActive ->
                                viewModel.updateGroup(group, description, isActive)
                            },
                            onViewRoles = { onNavigateToRoles(group.name) }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Group")
                }
            }

            if (showAddDialog) {
                AddGroupDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { name, description ->
                        viewModel.createGroup(name, description)
                        showAddDialog = false
                    }
                )
            }

            if (state.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun GroupCard(
    group: GOGroup,
    onUpdateGroup: (description: String?, isActive: Boolean) -> Unit,
    onViewRoles: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium
            )
            group.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Switch(
                    checked = group.isActive,
                    onCheckedChange = { onUpdateGroup(group.description, it) }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showEditDialog = true }) {
                        Text("Edit")
                    }
                    Button(onClick = onViewRoles) {
                        Text("View Roles")
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditGroupDialog(
            group = group,
            onDismiss = { showEditDialog = false },
            onConfirm = { description ->
                onUpdateGroup(description, group.isActive)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, description.takeIf { it.isNotBlank() }) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
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
private fun EditGroupDialog(
    group: GOGroup,
    onDismiss: () -> Unit,
    onConfirm: (description: String?) -> Unit
) {
    var description by remember { mutableStateOf(group.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Group") },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(description.takeIf { it.isNotBlank() }) }) {
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