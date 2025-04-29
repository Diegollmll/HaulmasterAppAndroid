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
import app.forku.domain.model.gogroup.GOGroupRole
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorBanner

@Composable
fun GroupRoleManagementScreen(
    groupName: String,
    viewModel: GroupRoleManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupName) {
        viewModel.loadRolesForGroup(groupName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Roles for Group: $groupName",
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
                items(state.roles) { role ->
                    RoleCard(
                        role = role,
                        onUpdateStatus = { isActive ->
                            viewModel.updateRoleStatus(role, isActive)
                        },
                        onRemove = {
                            viewModel.removeRoleFromGroup(role.groupName, role.roleName)
                        }
                    )
                }
            }

            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Role")
            }
        }

        if (showAddDialog) {
            AddRoleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { roleName ->
                    viewModel.assignRoleToGroup(groupName, roleName)
                    showAddDialog = false
                }
            )
        }

        if (state.isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun RoleCard(
    role: GOGroupRole,
    onUpdateStatus: (Boolean) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = role.roleName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Created: ${role.createdAt?.substringBefore('T') ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = role.isActive,
                    onCheckedChange = onUpdateStatus
                )
                TextButton(
                    onClick = onRemove,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun AddRoleDialog(
    onDismiss: () -> Unit,
    onConfirm: (roleName: String) -> Unit
) {
    var roleName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Role") },
        text = {
            OutlinedTextField(
                value = roleName,
                onValueChange = { roleName = it },
                label = { Text("Role Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(roleName) },
                enabled = roleName.isNotBlank()
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