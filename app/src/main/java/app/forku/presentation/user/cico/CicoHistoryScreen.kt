package app.forku.presentation.user.cico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CicoHistoryScreen(
    viewModel: CicoHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    operatorId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    // Calculate if we should load more items
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= listState.layoutInfo.totalItemsCount - 2
        }
    }
    
    // Trigger load more when we're close to the end
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.isLoading && !state.isLoadingMore && state.hasMoreItems) {
            viewModel.loadNextPage()
        }
    }

    // Load initial data with operatorId
    LaunchedEffect(operatorId) {
        android.util.Log.d("appflow", "CicoHistoryScreen LaunchedEffect operatorId: $operatorId")
        if(operatorId != null) {
            viewModel.setSelectedOperator(operatorId)
            viewModel.setDropdownExpanded(false)
        }
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = if (operatorId != null) "Operator CICO History" else "CICO History",
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (state.isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter by Operator:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ExposedDropdownMenuBox(
                            expanded = state.dropdownExpanded,
                            onExpandedChange = { viewModel.setDropdownExpanded(it) }
                        ) {
                            TextField(
                                value = state.operators.find { it.id == state.selectedOperatorId }?.name ?: "All Operators",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.dropdownExpanded) },
                                modifier = Modifier.menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = state.dropdownExpanded,
                                onDismissRequest = { viewModel.setDropdownExpanded(false) }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Operators") },
                                    onClick = {
                                        viewModel.setSelectedOperator(null)
                                        viewModel.setDropdownExpanded(false)
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                state.operators.forEach { operator ->
                                    DropdownMenuItem(
                                        text = { Text(operator.name) },
                                        onClick = {
                                            viewModel.setSelectedOperator(operator.id)
                                            viewModel.setDropdownExpanded(false)
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                when {
                    state.isLoading -> LoadingOverlay()
                    state.error != null -> ErrorScreen(
                        message = state.error ?: "Unknown error occurred",
                        onRetry = {
                            android.util.Log.d("appflow", "CicoHistoryScreen onRetry called")
                            viewModel.loadCicoHistory(operatorId)
                        }
                    )
                    else -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.filteredHistory) { entry ->
                                    //CicoHistoryItem(entry, state.isAdmin)
                                    CicoHistoryItem(entry, true)
                                }
                            }
                            
                            // Show loading indicator at bottom while loading more
                            if (state.isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        networkManager = networkManager
    )
}

@Composable
private fun CicoHistoryItem(entry: CicoEntry, showOperator: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = entry.vehicleName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (showOperator) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Operator: ${entry.operatorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Check In: ${entry.checkInTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Check Out: ${entry.checkOutTime ?: "In Progress"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.checkOutTime == null) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.typography.bodyMedium.color
                )
            }
        }
    }
} 