package app.forku.presentation.system

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.site.SitesViewModel
import app.forku.presentation.business.BusinessesViewModel
import android.util.Log
import app.forku.core.auth.UserRoleManager
import app.forku.domain.model.user.UserRole

@Composable
fun UserPreferencesSetupScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    userRoleManager: UserRoleManager,
    onSetupComplete: (() -> Unit)? = null
) {
    val systemSettingsViewModel: SystemSettingsViewModel = hiltViewModel()
    val sitesViewModel: SitesViewModel = hiltViewModel()
    val businessesViewModel: BusinessesViewModel = hiltViewModel()
    
    // Get current user to determine correct dashboard
    val currentUser by systemSettingsViewModel.currentUser.collectAsState()
    
    // States
    val sitesUiState by sitesViewModel.uiState.collectAsState()
    val businessesUiState by businessesViewModel.uiState.collectAsState()
    val isLoading by systemSettingsViewModel.isLoading.collectAsState()
    val message by systemSettingsViewModel.message.collectAsState()
    
    // Local state for the setup form
    var selectedBusinessId by remember { mutableStateOf<String?>(null) }
    var selectedSiteId by remember { mutableStateOf<String?>(null) }
    var showBusinessDropdown by remember { mutableStateOf(false) }
    var showSiteDropdown by remember { mutableStateOf(false) }
    
    val selectedBusiness = businessesUiState.businesses.find { it.id == selectedBusinessId }
    val selectedSite = sitesUiState.sites.find { it.id == selectedSiteId }
    
    // Auto-select if only one option available
    LaunchedEffect(businessesUiState.businesses) {
        if (businessesUiState.businesses.size == 1 && selectedBusinessId == null) {
            selectedBusinessId = businessesUiState.businesses.first().id
            Log.d("UserPreferencesSetup", "Auto-selected single business: ${businessesUiState.businesses.first().name}")
        }
    }
    
    LaunchedEffect(sitesUiState.sites, selectedBusinessId) {
        if (sitesUiState.sites.size == 1 && selectedSiteId == null && selectedBusinessId != null) {
            selectedSiteId = sitesUiState.sites.first().id
            Log.d("UserPreferencesSetup", "Auto-selected single site: ${sitesUiState.sites.first().name}")
        }
    }
    
    // Load data when screen loads
    LaunchedEffect(Unit) {
        sitesViewModel.loadUserAssignedSites()
        businessesViewModel.loadUserAssignedBusinesses()
    }
    
    // Handle setup completion
    LaunchedEffect(message, currentUser) {
        if (message?.contains("successfully") == true) {
            kotlinx.coroutines.delay(1500) // Show success message briefly
            
            // Use custom callback if provided, otherwise navigate based on user role
            if (onSetupComplete != null) {
                onSetupComplete()
            } else {
                // Determine correct dashboard based on user role
                val dashboardRoute = currentUser?.role?.let { role ->
                    userRoleManager.getDashboardRoute(role)
                } ?: "dashboard" // fallback to regular dashboard
                
                Log.d("UserPreferencesSetup", "Navigating to dashboard: $dashboardRoute for role: ${currentUser?.role}")
                
                navController.navigate(dashboardRoute) {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Setup Your Preferences",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Welcome to ForkU!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To get started, please select your business and site. This helps us show you the right vehicles and information.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Setup Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Setup Form",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Loading indicator
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }
                    
                    // Message display
                    message?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.contains("success")) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        ) {
                            Text(
                                text = msg,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (msg.contains("success")) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        }
                    }
                    
                    // Business Selection
                    Text(
                        text = "1. Select Your Business",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = selectedBusiness?.name ?: "Select Business",
                        onValueChange = {},
                        label = { Text("Business") },
                        readOnly = true,
                        enabled = !businessesUiState.isLoading && !isLoading,
                        trailingIcon = {
                            IconButton(onClick = { showBusinessDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Business")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = if (selectedBusinessId != null) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showBusinessDropdown,
                        onDismissRequest = { showBusinessDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        businessesUiState.businesses.forEach { business ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(business.name)
                                        Text(
                                            text = "(${business.id.take(8)}...)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedBusinessId = business.id
                                    showBusinessDropdown = false
                                    // Clear site selection when business changes
                                    selectedSiteId = null
                                }
                            )
                        }
                        
                        if (businessesUiState.businesses.isEmpty() && !businessesUiState.isLoading) {
                            DropdownMenuItem(
                                text = { Text("No businesses assigned") },
                                onClick = { showBusinessDropdown = false },
                                enabled = false
                            )
                        }
                    }
                    
                    Text(
                        text = "Available businesses: ${businessesUiState.businesses.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Site Selection
                    Text(
                        text = "2. Select Your Site",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = selectedSite?.name ?: "Select Site",
                        onValueChange = {},
                        label = { Text("Site") },
                        readOnly = true,
                        enabled = !sitesUiState.isLoading && !isLoading && selectedBusinessId != null,
                        trailingIcon = {
                            IconButton(
                                onClick = { 
                                    if (selectedBusinessId != null) {
                                        showSiteDropdown = true 
                                    }
                                },
                                enabled = selectedBusinessId != null
                            ) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Site")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = if (selectedSiteId != null) {
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        } else {
                            OutlinedTextFieldDefaults.colors()
                        }
                    )
                    
                    DropdownMenu(
                        expanded = showSiteDropdown,
                        onDismissRequest = { showSiteDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        sitesUiState.sites.forEach { site ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(site.name)
                                        Text(
                                            text = "(${site.id.take(8)}...)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSiteId = site.id
                                    showSiteDropdown = false
                                }
                            )
                        }
                        
                        if (sitesUiState.sites.isEmpty() && !sitesUiState.isLoading) {
                            DropdownMenuItem(
                                text = { Text("No sites assigned") },
                                onClick = { showSiteDropdown = false },
                                enabled = false
                            )
                        }
                    }
                    
                    if (selectedBusinessId == null) {
                        Text(
                            text = "Please select a business first",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Available sites: ${sitesUiState.sites.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    // Setup Button
                    val canSetup = selectedBusinessId != null && selectedSiteId != null
                    
                    Button(
                        onClick = {
                            if (canSetup) {
                                Log.d("UserPreferencesSetup", "Creating preferences with Business: $selectedBusinessId, Site: $selectedSiteId")
                                systemSettingsViewModel.createPreferencesWithBusinessAndSite(selectedBusinessId!!, selectedSiteId!!)
                            }
                        },
                        enabled = canSetup && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isLoading) "Setting up..." else "Complete Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    if (!canSetup) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please select both business and site to continue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Advanced Settings button
            TextButton(
                onClick = { 
                    navController.navigate("system_settings")
                }
            ) {
                Text("Advanced Settings")
            }
        }
    }
} 