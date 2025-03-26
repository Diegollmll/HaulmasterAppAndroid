package app.forku.presentation.certification.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.certification.CertificationStatus
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationDetailScreen(
    viewModel: CertificationDetailViewModel = hiltViewModel(),
    certificationId: String,
    navController: NavController,
    networkManager: NetworkConnectivityManager
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(certificationId) {
        viewModel.loadCertification(certificationId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this certification?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCertification()
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Certification Details",
        topBarActions = {
            IconButton(onClick = {
                navController.navigate(
                    Screen.CertificationEdit.createRoute(certificationId)
                )
            }) {
                Icon(Icons.Default.Edit, "Edit")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, "Delete")
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
                        onRetry = { viewModel.loadCertification(certificationId) }
                    )
                    state.certification != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = state.certification!!.name,
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        StatusChip(status = state.certification!!.status)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    state.certification!!.description?.let { description ->
                                        Text(
                                            text = description,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    InfoRow("Issuer", state.certification!!.issuer)
                                    InfoRow("Issue Date", state.certification!!.issuedDate)
                                    InfoRow(
                                        "Expiry Date",
                                        state.certification!!.expiryDate ?: "No expiry date"
                                    )
                                    InfoRow(
                                        "Certification Code",
                                        state.certification!!.certificationCode ?: "No code"
                                    )

                                    state.certification!!.documentUrl?.let { url ->
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { /* Handle document view */ },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("View Document")
                                        }
                                    }
                                }
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

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
} 