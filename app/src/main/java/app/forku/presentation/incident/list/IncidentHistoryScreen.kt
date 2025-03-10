package app.forku.presentation.incident.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import androidx.navigation.NavController
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentHistoryScreen(
    viewModel: IncidentHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit,
    navController: NavController
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Incident Reports",
        content = { padding ->
            when {
                state.isLoading -> LoadingOverlay()
                state.error != null -> ErrorScreen(
                    message = state.error ?: "Unknown error occurred",
                    onRetry = { viewModel.loadIncidents() }
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
                            IncidentHistoryItem(
                                incident = incident,
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
        }
    )
}

@Composable
private fun IncidentHistoryItem(
    incident: IncidentItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small
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
                    text = incident.type,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = getRelativeTimeSpanString(incident.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 