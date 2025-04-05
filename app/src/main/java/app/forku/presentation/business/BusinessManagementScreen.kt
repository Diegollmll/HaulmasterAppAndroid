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
import app.forku.presentation.dashboard.BusinessStatus
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.vector.ImageVector
import android.util.Log
import androidx.compose.ui.text.style.TextAlign
import app.forku.domain.model.user.UserRole


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BusinessManagementScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
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
        if (currentUser.value?.role != UserRole.SUPERADMIN) {
            navController.navigateUp()
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = true,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Business Management",
        networkManager = networkManager
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
                                    containerColor = MaterialTheme.colorScheme.primary
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
                                BusinessCard(business)
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
private fun BusinessCard(business: Business) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (business.status) {
                BusinessStatus.ACTIVE -> Color.White
                BusinessStatus.PENDING -> Color(0xFFFFF3E0)
                BusinessStatus.SUSPENDED -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = business.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = { },
                    label = { Text(business.status.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (business.status) {
                            BusinessStatus.ACTIVE -> Color(0xFFE8F5E9)
                            BusinessStatus.PENDING -> Color(0xFFFFF3E0)
                            BusinessStatus.SUSPENDED -> Color(0xFFFFEBEE)
                        }
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    icon = Icons.Default.Person,
                    value = business.totalUsers.toString(),
                    label = "Users"
                )
                InfoItem(
                    icon = Icons.Default.DirectionsCar,
                    value = business.totalVehicles.toString(),
                    label = "Vehicles"
                )
            }
        }
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