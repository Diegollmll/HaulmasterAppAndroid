package app.forku.presentation.certification.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.domain.model.certification.Certification
import app.forku.domain.model.certification.CertificationStatus
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.core.network.NetworkConnectivityManager
import androidx.navigation.NavController
import app.forku.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationsScreen(
    viewModel: CertificationsViewModel = hiltViewModel(),
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    userId: String? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = if (userId != null) "User Certifications" else "All Certifications",
        topBarActions = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CertificationCreate.route) }
            ) {
                Icon(Icons.Default.Add, "Add Certification")
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    state.isLoading -> LoadingOverlay()
                    state.error != null -> ErrorScreen(
                        message = state.error!!,
                        onRetry = { viewModel.loadCertifications(userId) }
                    )
                    state.certifications.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No certifications found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.certifications) { certification ->
                                CertificationCard(
                                    certification = certification,
                                    onDelete = { viewModel.deleteCertification(certification.id) },
                                    onClick = {
                                        navController.navigate(
                                            Screen.CertificationDetail.createRoute(certification.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (state.isDeleting) {
                    LoadingOverlay()
                }
            }
        },
        networkManager = networkManager
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CertificationCard(
    certification: Certification,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = certification.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                StatusChip(status = certification.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            certification.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Issued by ${certification.issuer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Issued: ${certification.issuedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: CertificationStatus) {
    val (backgroundColor, contentColor) = when (status) {
        CertificationStatus.ACTIVE -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        CertificationStatus.EXPIRED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        CertificationStatus.PENDING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        CertificationStatus.REVOKED -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
    }

    Surface(
        color = backgroundColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
} 