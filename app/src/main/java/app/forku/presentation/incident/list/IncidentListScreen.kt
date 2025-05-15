package app.forku.presentation.incident.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.IncidentCard
import app.forku.domain.model.incident.IncidentStatus

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun IncidentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    userId: String? = null,
    source: String? = null,
    tokenErrorHandler: TokenErrorHandler,
    viewModel: IncidentListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(userId, source) {
        viewModel.loadIncidents(userId, source)
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { viewModel.loadIncidents(userId, source) }
    )

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = when {
            userId != null -> "User Incidents"
            else -> "Incidents List"
        },
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    state.error != null -> ErrorScreen(
                        message = state.error ?: "Unknown error occurred",
                        onRetry = { viewModel.loadIncidents(userId, source) }
                    )
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            items(
                                items = state.incidents,
                                key = { it.id }
                            ) { incident ->
                                IncidentCard(
                                    type = incident.type,
                                    date = getRelativeTimeSpanString(incident.date),
                                    description = incident.description,
                                    status = try {
                                        IncidentStatus.valueOf(incident.status)
                                    } catch (e: IllegalArgumentException) {
                                        null
                                    },
                                    creatorName = incident.creatorName,
                                    onClick = {
                                        navController.navigate(
                                            Screen.IncidentDetail.route.replace(
                                                "{incidentId}",
                                                incident.id
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = state.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    )
} 