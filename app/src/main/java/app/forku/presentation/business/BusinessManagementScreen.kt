package app.forku.presentation.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.dashboard.SuperAdminDashboardState
import app.forku.presentation.dashboard.Business
import app.forku.domain.model.business.BusinessStatus
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.vector.ImageVector
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.colorResource
import app.forku.R
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.core.auth.TokenErrorHandler


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BusinessManagementScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: BusinessManagementViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState()
    val currentUser = viewModel.currentUser.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.value.isLoading,
        onRefresh = { viewModel.loadBusinesses() }
    )

    LaunchedEffect(Unit) {
        Log.d("BusinessManagement", "Screen launched, current user role: ${currentUser.value?.role}")
        if (currentUser.value?.role != UserRole.SYSTEM_OWNER && currentUser.value?.role != UserRole.SUPERADMIN) {
            navController.navigateUp()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Business Management",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                state.value.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.value.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.value.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadBusinesses() }
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Add Business Button
                        item {
                            Button(
                                onClick = { viewModel.showAddBusinessDialog() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.primary_blue),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add New Business")
                            }
                        }

                        // Statistics
                        item {
                            BusinessStatistics(state.value)
                        }

                        // Business List
                        if (state.value.businesses.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No businesses found",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(state.value.businesses) { business ->
                                BusinessCard(
                                    business = business,
                                    onStatusChange = { business: Business, newStatus: BusinessStatus -> 
                                        viewModel.updateBusinessStatus(business, newStatus)
                                    },
                                    onAssignUsers = { b -> viewModel.showAssignUsersDialog(b) },
                                    currentUser = currentUser.value,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }

            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = state.value.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Add Business Dialog
    if (state.value.showAddBusinessDialog) {
        AddBusinessDialog(
            onDismiss = { viewModel.hideAddBusinessDialog() },
            onConfirm = { name -> viewModel.addBusiness(name) }
        )
    }

    // Assign Users Dialog
    if (state.value.showAssignUsersDialog) {
        AssignUsersDialog(
            availableUsers = state.value.availableUsers,
            selectedUserIds = state.value.selectedUsers,
            onUserToggle = { viewModel.toggleUserSelection(it) },
            onDismiss = { viewModel.hideAssignUsersDialog() },
            onSave = { viewModel.saveUserAssignments() }
        )
    }
}

@Composable
private fun BusinessStatistics(state: BusinessManagementState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.Business,
                    count = state.totalBusinesses,
                    label = "Total"
                )
                StatItem(
                    icon = Icons.Default.PendingActions,
                    count = state.pendingApprovals,
                    label = "Pending"
                )
                StatItem(
                    icon = Icons.Default.Person,
                    count = state.unassignedUsers,
                    label = "Unassigned"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun BusinessCard(
    business: Business,
    onStatusChange: (Business, BusinessStatus) -> Unit = { _, _ -> },
    onAssignUsers: (Business) -> Unit = { },
    currentUser: User? = null,
    navController: NavController,
    viewModel: BusinessManagementViewModel = hiltViewModel()
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showAssignSuperAdminDialog by remember { mutableStateOf(false) }
    val businessSuperAdmins by viewModel.businessSuperAdmins.collectAsState()
    val currentSuperAdmin = businessSuperAdmins[business.id]
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Todos los estados usan fondo blanco
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Business name and counts
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = business.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Users count with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${business.totalUsers}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Users",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }
                    
                    // Vehicles count with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${business.totalVehicles}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Vehicles",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                    }

                    // SuperAdmin info
                    if (currentSuperAdmin != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "SuperAdmin",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "${currentSuperAdmin.firstName} ${currentSuperAdmin.lastName}",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Status and actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status chip
                Box {
                    if (showStatusMenu) {
                        // Show chip with text and icon when menu is open
                        AssistChip(
                            onClick = { 
                                if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                                    showStatusMenu = false
                                }
                            },
                            label = { Text(business.status.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (business.status) {
                                        BusinessStatus.ACTIVE -> Icons.Default.CheckCircle
                                        BusinessStatus.PENDING -> Icons.Default.Pending
                                        BusinessStatus.SUSPENDED -> Icons.Default.Block
                                    },
                                    contentDescription = null,
                                    tint = when (business.status) {
                                        BusinessStatus.ACTIVE -> Color(0xFF4CAF50)
                                        BusinessStatus.PENDING -> Color(0xFFFFA726)
                                        BusinessStatus.SUSPENDED -> Color(0xFFF44336)
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (business.status) {
                                    BusinessStatus.ACTIVE -> Color(0xFFE8F5E9)
                                    BusinessStatus.PENDING -> Color(0xFFFFF3E0)
                                    BusinessStatus.SUSPENDED -> Color(0xFFFFEBEE)
                                }
                            ),
                            trailingIcon = if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Change status",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    } else {
                        // Show only icon when menu is closed
                        IconButton(
                            onClick = { 
                                if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                                    showStatusMenu = true 
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = when (business.status) {
                                    BusinessStatus.ACTIVE -> Icons.Default.CheckCircle
                                    BusinessStatus.PENDING -> Icons.Default.Pending
                                    BusinessStatus.SUSPENDED -> Icons.Default.Block
                                },
                                contentDescription = business.status.name,
                                tint = when (business.status) {
                                    BusinessStatus.ACTIVE -> Color(0xFF4CAF50)
                                    BusinessStatus.PENDING -> Color(0xFFFFA726)
                                    BusinessStatus.SUSPENDED -> Color(0xFFF44336)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            BusinessStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        onStatusChange(business, status)
                                        showStatusMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (status) {
                                                BusinessStatus.ACTIVE -> Icons.Default.CheckCircle
                                                BusinessStatus.PENDING -> Icons.Default.Pending
                                                BusinessStatus.SUSPENDED -> Icons.Default.Block
                                            },
                                            contentDescription = null,
                                            tint = when (status) {
                                                BusinessStatus.ACTIVE -> Color(0xFF4CAF50)
                                                BusinessStatus.PENDING -> Color(0xFFFFA726)
                                                BusinessStatus.SUSPENDED -> Color(0xFFF44336)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // Action buttons
                if (currentUser?.role == UserRole.SYSTEM_OWNER) {
                    IconButton(
                        onClick = { showAssignSuperAdminDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Assign SuperAdmin",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (currentUser?.role == UserRole.SYSTEM_OWNER || 
                    (currentUser?.role == UserRole.SUPERADMIN && currentSuperAdmin?.id == currentUser.id)) {
                    IconButton(
                        onClick = { onAssignUsers(business) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Assign Users",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Add Sites Management Button
                    IconButton(
                        onClick = { 
                            navController.navigate("sites/${business.id}") 
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Manage Sites",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showAssignSuperAdminDialog) {
        AssignSuperAdminDialog(
            business = business,
            onDismiss = { showAssignSuperAdminDialog = false },
            onAssign = { superAdminId ->
                viewModel.assignSuperAdmin(business.id, superAdminId)
                showAssignSuperAdminDialog = false
            }
        )
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Text(
            text = "$value $label",
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun AddBusinessDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var businessName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add New Business")
        },
        text = {
            OutlinedTextField(
                value = businessName,
                onValueChange = { 
                    android.util.Log.d("BusinessManagement", "Business name changed to: $it")
                    businessName = it 
                },
                label = { Text("Business Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    android.util.Log.d("BusinessManagement", "Add button clicked with name: $businessName")
                    onConfirm(businessName)
                },
                enabled = businessName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = { 
                android.util.Log.d("BusinessManagement", "Cancel button clicked")
                onDismiss() 
            }) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignUsersDialog(
    availableUsers: List<User>,
    selectedUserIds: List<String>,
    onUserToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Users") },
        text = {
            if (availableUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No users available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column {
                    Text(
                        text = "Select or unselect users to assign/unassign them from this business:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn {
                        items(availableUsers) { user ->
                            val isHighPrivilegeRole = user.role == UserRole.SYSTEM_OWNER || user.role == UserRole.SUPERADMIN
                            val isAssigned = user.id in selectedUserIds
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(
                                        enabled = !isHighPrivilegeRole,
                                        onClick = { onUserToggle(user.id) }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isAssigned,
                                    onCheckedChange = { if (!isHighPrivilegeRole) onUserToggle(user.id) },
                                    enabled = !isHighPrivilegeRole
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${user.firstName} ${user.lastName}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        AssistChip(
                                            onClick = { },
                                            enabled = false,
                                            label = { 
                                                Text(
                                                    text = if (isAssigned) "Assigned" else "Unassigned",
                                                    fontSize = 12.sp
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (isAssigned) 
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else 
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignSuperAdminDialog(
    business: Business,
    onDismiss: () -> Unit,
    onAssign: (String) -> Unit,
    viewModel: BusinessManagementViewModel = hiltViewModel()
) {
    val superAdmins by viewModel.availableSuperAdmins.collectAsState()
    val businessSuperAdmins by viewModel.businessSuperAdmins.collectAsState()
    val currentSuperAdmin = businessSuperAdmins[business.id]
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign SuperAdmin to ${business.name}") },
        text = {
            Column {
                // "None" option to explicitly deassign SuperAdmin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAssign("") }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSuperAdmin == null,
                        onClick = { onAssign("") }
                    )
                    Text(
                        text = "None (No SuperAdmin)",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (currentSuperAdmin != null) {
                    Text(
                        text = "Current SuperAdmin:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (superAdmins.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No SuperAdmins available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn {
                        items(superAdmins) { superAdmin ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAssign(superAdmin.id) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentSuperAdmin != null && superAdmin.id == currentSuperAdmin.id,
                                    onClick = { onAssign(superAdmin.id) }
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "${superAdmin.firstName} ${superAdmin.lastName}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = superAdmin.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 