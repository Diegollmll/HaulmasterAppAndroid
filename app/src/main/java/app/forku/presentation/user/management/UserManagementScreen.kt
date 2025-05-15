package app.forku.presentation.user.management

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import app.forku.core.auth.TokenErrorHandler
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.presentation.dashboard.SystemOwnerDashboardState
import app.forku.presentation.navigation.Screen
import app.forku.presentation.navigation.Screen.Companion.withOperatorId


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    showAddUserDialogByDefault: Boolean = false,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState()
    val currentUser = viewModel.currentUser.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.value.isLoading,
        onRefresh = { viewModel.loadUsers() }
    )

    LaunchedEffect(Unit) {
        Log.d("UserManagement", "Screen launched, current user role: ${currentUser.value?.role}")
        if (currentUser.value?.role != UserRole.SYSTEM_OWNER && currentUser.value?.role != UserRole.SUPERADMIN) {
            navController.navigateUp()
        }
        if (showAddUserDialogByDefault) {
            viewModel.toggleAddUserDialog()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "User Management",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Stats Overview
                StatsOverview(state.value)

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                ActionButtons(
                    onAddUser = { viewModel.toggleAddUserDialog() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Users List
                UsersList(
                    users = state.value.users,
                    onUserClick = { user ->
                        navController.navigate(Screen.Profile.withOperatorId(user.id))
                    },
                    onRoleClick = { user ->
                        viewModel.showRoleDialog(user)
                    },
                    onDeleteClick = { userId ->
                        viewModel.deleteUser(userId)
                    },
                    onApproveClick = { user ->
                        viewModel.approveUser(user)
                    }
                )
            }

            // Pull to refresh indicator
            PullRefreshIndicator(
                refreshing = state.value.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Role Change Dialog
            if (state.value.showRoleDialog) {
                RoleChangeDialog(
                    user = state.value.selectedUser!!,
                    onDismiss = { viewModel.hideRoleDialog() },
                    onRoleSelected = { role ->
                        viewModel.updateUserRole(state.value.selectedUser!!, role)
                        viewModel.hideRoleDialog()
                    },
                    viewModel = viewModel
                )
            }

            // Add User Dialog
            if (state.value.showAddUserDialog) {
                AddUserDialog(
                    onDismiss = { viewModel.toggleAddUserDialog() },
                    onUserAdded = {
                        viewModel.toggleAddUserDialog()
                        viewModel.loadUsers()
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun StatsOverview(state: UserManagementState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Users Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Users",
                    value = state.totalUsers.toString(),
                    icon = Icons.Default.People
                )
                StatItem(
                    label = "Pending Approvals",
                    value = state.pendingApprovals.toString(),
                    icon = Icons.Default.PersonAdd
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(onAddUser: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAddUser,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add User")
        }
    }
}

@Composable
private fun UsersList(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onRoleClick: (User) -> Unit,
    onDeleteClick: (String) -> Unit,
    onApproveClick: (User) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            UserListItem(
                user = user,
                onClick = { onUserClick(user) },
                onRoleClick = { onRoleClick(user) },
                onDeleteClick = { onDeleteClick(user.id) },
                onApproveClick = { onApproveClick(user) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListItem(
    user: User,
    onClick: () -> Unit,
    onRoleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onApproveClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User name and email
            Text(
                text = user.fullName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Role and Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = user.role.name,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!user.isApproved) {
                        IconButton(
                            onClick = onApproveClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Approve User",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = onRoleClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Change Role",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Pending Approval status (if applicable)
            if (!user.isApproved) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Pending Approval",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun RoleChangeDialog(
    user: User,
    onDismiss: () -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val currentUser = viewModel.currentUser.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Role") },
        text = {
            Column {
                Text("Select new role for ${user.fullName}")
                Spacer(modifier = Modifier.height(16.dp))
                // Filter available roles based on current user's role
                val availableRoles = when (currentUser.value?.role) {
                    UserRole.SYSTEM_OWNER -> UserRole.values().filter { it != UserRole.SYSTEM_OWNER }
                    UserRole.SUPERADMIN -> listOf(UserRole.ADMIN, UserRole.OPERATOR)
                    else -> emptyList()
                }
                
                availableRoles.forEach { role ->
                    TextButton(
                        onClick = { onRoleSelected(role) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(role.name)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onUserAdded: () -> Unit,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.OPERATOR) }
    var isRoleMenuExpanded by remember { mutableStateOf(false) }
    val currentUser = viewModel.currentUser.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                
                // Role selection dropdown - show different options based on user role
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = isRoleMenuExpanded,
                        onExpandedChange = { isRoleMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = role.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRoleMenuExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = isRoleMenuExpanded,
                            onDismissRequest = { isRoleMenuExpanded = false }
                        ) {
                            // Filter available roles based on current user's role
                            val availableRoles = when (currentUser.value?.role) {
                                UserRole.SYSTEM_OWNER -> UserRole.values().filter { it != UserRole.SYSTEM_OWNER }
                                UserRole.SUPERADMIN -> listOf(UserRole.ADMIN, UserRole.OPERATOR)
                                else -> emptyList()
                            }
                            
                            availableRoles.forEach { userRole ->
                                DropdownMenuItem(
                                    text = { Text(userRole.name) },
                                    onClick = {
                                        role = userRole
                                        isRoleMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.registerUser(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password,
                        role = role
                    )
                    onUserAdded()
                },
                enabled = firstName.isNotBlank() && 
                         lastName.isNotBlank() && 
                         email.isNotBlank() && 
                         password.isNotBlank()
            ) {
                Text("Add User")
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
private fun AdminActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        color = Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun UserManagementSection(
    state: SystemOwnerDashboardState,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Add active connections metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Active Users",
                    value = state.activeConnections.toString(),
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminActionButton(
                    icon = Icons.Default.PersonAdd,
                    text = "Add User",
                    onClick = { navController.navigate(Screen.AddUser.route) }
                )
                AdminActionButton(
                    icon = Icons.Default.Group,
                    text = "Manage Roles",
                    onClick = { navController.navigate(Screen.RoleManagement.route) }
                )
            }
        }
    }
} 