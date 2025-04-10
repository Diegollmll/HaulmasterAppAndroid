package app.forku.presentation.countries

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.domain.model.country.Country
import app.forku.domain.model.country.State

@Composable
fun CountriesScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: CountriesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Countries & States Management",
        networkManager = networkManager
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Add Country Button
            Button(
                onClick = { viewModel.showAddCountryDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Country")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Countries List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.countries) { country ->
                    CountryCard(
                        country = country,
                        states = state.statesByCountry[country.id] ?: emptyList(),
                        onEditCountry = { viewModel.showEditCountryDialog(country) },
                        onDeleteCountry = { viewModel.deleteCountry(country.id) },
                        onToggleCountryActive = { viewModel.toggleCountryActive(country) },
                        onAddState = { viewModel.showAddStateDialog(country) },
                        onEditState = { viewModel.showEditStateDialog(it) },
                        onDeleteState = { viewModel.deleteState(it.id) },
                        onToggleStateActive = { viewModel.toggleStateActive(it) }
                    )
                }
            }

            // Add/Edit Country Dialog
            if (state.showCountryDialog) {
                CountryDialog(
                    country = state.selectedCountry,
                    onDismiss = { viewModel.hideCountryDialog() },
                    onSave = { name, code, phoneCode, currency, currencySymbol ->
                        if (state.selectedCountry != null) {
                            viewModel.updateCountry(
                                state.selectedCountry!!.copy(
                                    name = name,
                                    code = code,
                                    phoneCode = phoneCode,
                                    currency = currency,
                                    currencySymbol = currencySymbol
                                )
                            )
                        } else {
                            viewModel.createCountry(
                                name = name,
                                code = code,
                                phoneCode = phoneCode,
                                currency = currency,
                                currencySymbol = currencySymbol
                            )
                        }
                    }
                )
            }

            // Add/Edit State Dialog
            if (state.showStateDialog) {
                StateDialog(
                    state = state.selectedState,
                    onDismiss = { viewModel.hideStateDialog() },
                    onSave = { name, code ->
                        if (state.selectedState != null) {
                            viewModel.updateState(
                                state.selectedState!!.copy(
                                    name = name,
                                    code = code
                                )
                            )
                        } else {
                            state.selectedCountry?.let { country ->
                                viewModel.createState(
                                    countryId = country.id,
                                    name = name,
                                    code = code
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CountryCard(
    country: Country,
    states: List<State>,
    onEditCountry: () -> Unit,
    onDeleteCountry: () -> Unit,
    onToggleCountryActive: () -> Unit,
    onAddState: () -> Unit,
    onEditState: (State) -> Unit,
    onDeleteState: (State) -> Unit,
    onToggleStateActive: (State) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Log.d("CountriesScreen", "Rendering CountryCard for ${country.name} with ${states.size} states")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Country Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${country.code} (${country.phoneCode})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${country.currency} (${country.currencySymbol})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Row {
                    Switch(
                        checked = country.isActive,
                        onCheckedChange = { onToggleCountryActive() }
                    )
                    IconButton(onClick = onEditCountry) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteCountry) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    IconButton(
                        onClick = { 
                            expanded = !expanded
                            Log.d("CountriesScreen", "Toggled expansion for ${country.name} to $expanded")
                        }
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Show less" else "Show more"
                        )
                    }
                }
            }

            // States Section
            if (expanded) {
                Log.d("CountriesScreen", "Showing states for ${country.name}: ${states.size} states")
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "States/Provinces",
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = onAddState) {
                        Icon(Icons.Default.Add, contentDescription = "Add State")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (states.isEmpty()) {
                    Text(
                        text = "No states/provinces added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    states.forEach { state ->
                        StateItem(
                            state = state,
                            onEdit = { onEditState(state) },
                            onDelete = { onDeleteState(state) },
                            onToggleActive = { onToggleStateActive(state) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StateItem(
    state: State,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = state.code,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Row {
                Switch(
                    checked = state.isActive,
                    onCheckedChange = { onToggleActive() }
                )
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit State")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete State")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDialog(
    country: Country?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(country?.name ?: "") }
    var code by remember { mutableStateOf(country?.code ?: "") }
    var phoneCode by remember { mutableStateOf(country?.phoneCode ?: "") }
    var currency by remember { mutableStateOf(country?.currency ?: "") }
    var currencySymbol by remember { mutableStateOf(country?.currencySymbol ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (country == null) "Add Country" else "Edit Country") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Country Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Country Code (e.g., US)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneCode,
                    onValueChange = { phoneCode = it },
                    label = { Text("Phone Code (e.g., +1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Currency (e.g., USD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = currencySymbol,
                    onValueChange = { currencySymbol = it },
                    label = { Text("Currency Symbol (e.g., $)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, code, phoneCode, currency, currencySymbol)
                    onDismiss()
                },
                enabled = name.isNotBlank() && code.isNotBlank() && 
                         phoneCode.isNotBlank() && currency.isNotBlank() && 
                         currencySymbol.isNotBlank()
            ) {
                Text(if (country == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StateDialog(
    state: State?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(state?.name ?: "") }
    var code by remember { mutableStateOf(state?.code ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state == null) "Add State" else "Edit State") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("State Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("State Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, code)
                    onDismiss()
                },
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text(if (state == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 