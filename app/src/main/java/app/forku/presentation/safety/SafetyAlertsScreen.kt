package app.forku.presentation.safety

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.BusinessSiteFilters
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.core.auth.TokenErrorHandler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyAlertsScreen(
    viewModel: SafetyAlertsViewModel = hiltViewModel(),
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = true,
        viewModel = viewModel,
        topBarTitle = "Safety Alerts",
        networkManager = networkManager,
        onAppResume = { viewModel.loadSafetyAlerts() },
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Business and Site Filters
            BusinessSiteFilters(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onBusinessChanged = { businessId ->
                    viewModel.updateBusinessContext(businessId)
                },
                onSiteChanged = { siteId ->
                    viewModel.updateSiteContext(siteId)
                },
                showBusinessFilter = false, // ✅ Hide business filter
                isCollapsible = true, // ✅ Make filters collapsible
                initiallyExpanded = false, // ✅ Start collapsed
                businessContextManager = viewModel.businessContextManager,
                title = "Filter Safety Alerts by Context"
            )
            
            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                state.isLoading -> {
                    LoadingOverlay()
                }
                state.error != null -> {
                    ErrorScreen(
                        message = state.error ?: "",
                        onRetry = { viewModel.loadSafetyAlerts() }
                    )
                }
                state.alerts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text("No safety alerts found")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.alerts) { alert ->
                            SafetyAlertCard(
                                alert = alert,
                                onDelete = { viewModel.deleteSafetyAlert(alert) }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyAlertCard(
    alert: SafetyAlert,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium
                )
            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyMedium
            )
            if (alert.createdAt.isNotBlank()) {
                val formattedDate = try {
                    val instant = java.time.Instant.parse(alert.createdAt)
                    val formatter = java.time.format.DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
                        .withZone(java.time.ZoneId.systemDefault())
                    formatter.format(instant)
                } catch (e: Exception) {
                    alert.createdAt // fallback
                }
                Text(
                    text = "Created: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString
    }
}