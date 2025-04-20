package app.forku.presentation.site

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.domain.model.Site

@Composable
fun SitesScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    businessId: String,
    businessName: String,
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
        topBarTitle = "Sites - $businessName",
        networkManager = networkManager
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Site")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sites List
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.sites.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sites found for this business",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.sites) { site ->
                            SiteCard(
                                site = site,
                                onEdit = { viewModel.showEditSiteDialog(site) },
                                onDelete = { viewModel.showDeleteConfirmation(site) }
                            )
                        }
                    }
                }
            }

            // Add/Edit Site Dialog
            if (uiState.showDialog) {
                SiteDialog(
                    site = uiState.selectedSite,
                    businessId = businessId,
                    onDismiss = { viewModel.hideDialog() },
                    onSave = { site ->
                        if (site.id.isEmpty()) {
                            viewModel.createSite(businessId, site)
                        } else {
                            viewModel.updateSite(businessId, site)
                        }
                    }
                )
            }

            // Delete Confirmation Dialog
            if (uiState.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideDeleteConfirmation() },
                    title = { Text("Confirm Deletion") },
                    text = { 
                        Text("Are you sure you want to delete this site? This action cannot be undone.") 
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                uiState.selectedSite?.let { site ->
                                    viewModel.deleteSite(businessId, site.id)
                                }
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
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
}

@Composable
private fun SiteCard(
    site: Site,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = site.address,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!site.isActive) {
                        Text(
                            text = "Inactive",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
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
}

@Composable
private fun SiteDialog(
    site: Site?,
    businessId: String,
    onDismiss: () -> Unit,
    onSave: (Site) -> Unit
) {
    var name by remember { mutableStateOf(site?.name ?: "") }
    var address by remember { mutableStateOf(site?.address ?: "") }
    var latitude by remember { mutableStateOf(site?.latitude?.toString() ?: "0.0") }
    var longitude by remember { mutableStateOf(site?.longitude?.toString() ?: "0.0") }
    var isActive by remember { mutableStateOf(site?.isActive ?: true) }

    val isValid = name.isNotBlank() && address.isNotBlank() &&
                  latitude.toDoubleOrNull() != null && longitude.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (site == null) "Add Site" else "Edit Site") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text("Active")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newSite = Site(
                        id = site?.id ?: "",
                        name = name,
                        address = address,
                        businessId = businessId,
                        latitude = latitude.toDoubleOrNull() ?: 0.0,
                        longitude = longitude.toDoubleOrNull() ?: 0.0,
                        isActive = isActive,
                        createdAt = site?.createdAt ?: "",
                        updatedAt = site?.updatedAt ?: ""
                    )
                    onSave(newSite)
                    onDismiss()
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 