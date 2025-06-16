package app.forku.presentation.user.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.auth.UserRoleManager
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.dashboard.OperatorSessionInfo
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar
import app.forku.presentation.navigation.Screen

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OperatorsListScreen(
    navController: NavController,
    viewModel: OperatorsListViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading && state.isRefreshing,
        onRefresh = { viewModel.loadOperators(true) }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Users",
        showBottomBar = false,
        onRefresh = { viewModel.loadOperators(true) },
        showLoadingOnRefresh = false,
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Business context header
                item {
                    BusinessContextHeader(
                        businessId = state.currentBusinessId,
                        siteId = state.currentSiteId,
                        hasBusinessContext = state.hasBusinessContext,
                        totalUsers = state.operators.size,
                        activeUsers = state.operators.count { it.isActive }
                    )
                }
                
                if (state.isLoading && !state.isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (state.operators.isEmpty() && !state.isLoading) {
                    item {
                        NoUsersMessage(
                            hasBusinessContext = state.hasBusinessContext,
                            onRefresh = { viewModel.refreshBusinessContext() }
                        )
                    }
                } else {
                    items(state.operators) { operator ->
                        OperatorItem(
                            operator = operator,
                            onClick = { 
                                navController.navigate(Screen.Profile.createRoute(operator.userId))
                            }
                        )
                    }
                }
                
                state.error?.let { error ->
                    item {
                        ErrorMessage(
                            message = error,
                            onRetry = { viewModel.refreshBusinessContext() }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isLoading && state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun BusinessContextHeader(
    businessId: String?,
    siteId: String?,
    hasBusinessContext: Boolean,
    totalUsers: Int,
    activeUsers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Business Context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (hasBusinessContext && !businessId.isNullOrBlank()) {
                Text(
                    text = "Business ID: $businessId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!siteId.isNullOrBlank()) {
                    Text(
                        text = "Site ID: $siteId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Site: All Sites",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Users: $totalUsers",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Active: $activeUsers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activeUsers > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "No business context available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NoUsersMessage(
    hasBusinessContext: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (hasBusinessContext) {
                    "No users assigned to this business"
                } else {
                    "No business context available"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (hasBusinessContext) {
                    "This business doesn't have any users assigned yet."
                } else {
                    "Unable to determine current business context."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRefresh
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun OperatorItem(
    operator: OperatorSessionInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                avatarData = getUserAvatarData(
                    operator.name.split(" ").firstOrNull(),
                    operator.name.split(" ").drop(1).firstOrNull(),
                    operator.image
                ),
                size = 48.dp,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = operator.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "@${operator.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = UserRoleManager.toDisplayString(operator.role),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                                         .background(
                         if (operator.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                         CircleShape
                     )
            )
        }
    }
} 