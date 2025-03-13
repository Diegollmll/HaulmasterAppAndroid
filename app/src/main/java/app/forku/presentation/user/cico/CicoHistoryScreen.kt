package app.forku.presentation.user.cico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CicoHistoryScreen(
    viewModel: CicoHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "CICO History",
        content = { padding ->
            when {
                state.isLoading -> LoadingOverlay()
                state.error != null -> ErrorScreen(
                    message = state.error ?: "Unknown error occurred",
                    onRetry = { viewModel.loadCicoHistory() }
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        items(state.cicoHistory) { entry ->
                            CicoHistoryItem(entry)
                        }
                    }
                }
            }
        },
        networkManager = networkManager
    )
}

@Composable
private fun CicoHistoryItem(entry: CicoEntry) {
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