package app.forku.presentation.sites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.Site
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.site.SitesViewModel

@Composable
fun SitesScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    businessId: String,
    viewModel: SitesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(businessId) {
        viewModel.loadSites(businessId)
    }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Sites Management",
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Add Site Button
                        Button(
                            onClick = { viewModel.showAddSiteDialog() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add New Site")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sites List
                        if (uiState.sites.isEmpty()) {
                            Text(
                                text = "No sites found",
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.sites) { site ->
                                    SiteCard(
                                        site = site,
                                        onEdit = { viewModel.showEditSiteDialog(it) },
                                        onDelete = { viewModel.showDeleteConfirmation(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add/Edit Site Dialog
        if (uiState.showDialog) {
            SiteDialog(
                site = uiState.selectedSite,
                onDismiss = { viewModel.hideDialog() },
                onSave = { site ->
                    if (uiState.selectedSite != null) {
                        viewModel.updateSite(businessId, site)
                    } else {
                        viewModel.createSite(businessId, site)
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.hideDeleteConfirmation() },
                title = { Text("Delete Site") },
                text = { Text("Are you sure you want to delete this site?") },
                confirmButton = {
                    Button(
                        onClick = {
                            uiState.selectedSite?.let { site ->
                                viewModel.deleteSite(businessId, site.id)
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteCard(
    site: Site,
    onEdit: (Site) -> Unit,
    onDelete: (Site) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onEdit(site) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = site.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = site.address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { onEdit(site) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete(site) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SiteDialog(
    site: Site?,
    onDismiss: () -> Unit,
    onSave: (Site) -> Unit
) {
    var name by remember { mutableStateOf(site?.name ?: "") }
    var address by remember { mutableStateOf(site?.address ?: "") }
    var latitude by remember { mutableStateOf(site?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(site?.longitude?.toString() ?: "") }

    // Validation states
    var nameError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var latLongError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (site == null) "Add New Site" else "Edit Site") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Name*") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name is required") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { 
                        address = it
                        addressError = it.isBlank()
                    },
                    label = { Text("Address*") },
                    isError = addressError,
                    supportingText = if (addressError) {
                        { Text("Address is required") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = latitude,
                    onValueChange = { 
                        latitude = it
                        latLongError = (it.isNotEmpty() && it.toDoubleOrNull() == null) ||
                                     (it.isNotEmpty() && longitude.isEmpty()) ||
                                     (it.isEmpty() && longitude.isNotEmpty())
                    },
                    label = { Text("Latitude (optional)") },
                    isError = latLongError,
                    supportingText = if (latLongError) {
                        { Text("Both latitude and longitude must be valid numbers if provided") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = longitude,
                    onValueChange = { 
                        longitude = it
                        latLongError = (it.isNotEmpty() && it.toDoubleOrNull() == null) ||
                                     (it.isNotEmpty() && latitude.isEmpty()) ||
                                     (it.isEmpty() && latitude.isNotEmpty())
                    },
                    label = { Text("Longitude (optional)") },
                    isError = latLongError,
                    supportingText = if (latLongError) {
                        { Text("Both latitude and longitude must be valid numbers if provided") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (latLongError) {
                    Text(
                        text = "Latitude and longitude must be provided together",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newSite = Site(
                        id = site?.id ?: "",
                        name = name,
                        address = address,
                        businessId = site?.businessId ?: "",
                        latitude = if (latitude.isNotEmpty() && longitude.isNotEmpty()) 
                            latitude.toDoubleOrNull() ?: 0.0 else 0.0,
                        longitude = if (latitude.isNotEmpty() && longitude.isNotEmpty()) 
                            longitude.toDoubleOrNull() ?: 0.0 else 0.0,
                        isActive = site?.isActive ?: true,
                        createdAt = site?.createdAt ?: "",
                        updatedAt = site?.updatedAt ?: ""
                    )
                    onSave(newSite)
                },
                enabled = name.isNotBlank() && 
                         address.isNotBlank() && 
                         !latLongError &&
                         ((latitude.isEmpty() && longitude.isEmpty()) || 
                          (latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null))
            ) {
                Text(if (site == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 