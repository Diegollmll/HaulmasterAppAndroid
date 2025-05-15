package app.forku.presentation.incident.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.core.auth.TokenErrorHandler

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    viewModel: IncidentDetailViewModel = hiltViewModel(),
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { viewModel.loadIncidentDetail(incidentId) }
    )

    // Cargar los detalles cuando se inicia la pantalla
    LaunchedEffect(incidentId) {
        viewModel.loadIncidentDetail(incidentId)
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Incident Details",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
                    .padding(padding)
            ) {
                when {
                    state.isLoading -> LoadingOverlay()
                    state.error != null -> ErrorScreen(
                        message = state.error ?: "Unknown error occurred",
                        onRetry = { viewModel.loadIncidentDetail(incidentId) }
                    )
                    state.incident != null -> {
                        val incident = state.incident
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = incident?.type ?: "",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = getRelativeTimeSpanString(incident?.date.toString()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "Description",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = incident?.description ?: "",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    
                                    if (incident?.location?.isNotEmpty() == true) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Location",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = incident.location,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    
                                    if (incident?.attachments?.isNotEmpty() == true) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Attachments",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // TODO: Implement attachments display
                                        Column {
                                            incident?.attachments?.forEach { attachment ->
                                                Text(
                                                    text = attachment,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
} 