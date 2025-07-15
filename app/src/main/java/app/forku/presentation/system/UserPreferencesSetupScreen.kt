package app.forku.presentation.system

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import app.forku.presentation.common.components.FeedbackBanner
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import app.forku.presentation.system.UserPreferencesSetupScreenViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserPreferencesSetupScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler,
    userRoleManager: UserRoleManager,
    showBackButton: Boolean = false,
    onSetupComplete: (() -> Unit)? = null
) {
    val viewModel: UserPreferencesSetupScreenViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val isLoading = state.isLoading
    val message = state.message
    val currentUser = state.currentUser
    val currentUserPreferences = state.currentUserPreferences
    val businesses = state.businesses
    val sites = state.sites

    // Local state for the setup form
    var selectedBusinessId by remember { mutableStateOf<String?>(null) }
    var selectedSiteId by remember { mutableStateOf<String?>(null) }
    var showBusinessDropdown by remember { mutableStateOf(false) }
    var showSiteDropdown by remember { mutableStateOf(false) }
    var showContactAdminDialog by remember { mutableStateOf(false) }
    var feedbackSubmitted by remember { mutableStateOf(false) }

    val selectedBusiness = businesses.find { it.id == selectedBusinessId }
    val selectedSite = sites.find { it.id == selectedSiteId }

    val hasNoBusinesses = !isLoading && businesses.isEmpty()
    val hasNoSites = !isLoading && sites.isEmpty() && selectedBusinessId != null

    val isRefreshing = isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            viewModel.loadUserAssignedSites()
            viewModel.loadUserAssignedBusinesses()
        }
    )

    // Pre-select values from current user preferences
    LaunchedEffect(currentUserPreferences, businesses, sites) {
        if (currentUserPreferences != null) {
            val effectiveBusinessId = currentUserPreferences.getEffectiveBusinessId()
            val effectiveSiteId = currentUserPreferences.getEffectiveSiteId()
            if (effectiveBusinessId != null && selectedBusinessId == null) {
                if (businesses.any { it.id == effectiveBusinessId }) {
                    selectedBusinessId = effectiveBusinessId
                }
            }
            if (effectiveSiteId != null && selectedSiteId == null) {
                if (sites.any { it.id == effectiveSiteId }) {
                    selectedSiteId = effectiveSiteId
                }
            }
        }
    }

    // Auto-select if only one option available
    LaunchedEffect(businesses, currentUserPreferences) {
        if (businesses.size == 1 && selectedBusinessId == null && currentUserPreferences == null) {
            selectedBusinessId = businesses.first().id
        }
    }
    LaunchedEffect(sites, selectedBusinessId, currentUserPreferences) {
        if (sites.size == 1 && selectedSiteId == null && selectedBusinessId != null && currentUserPreferences == null) {
            selectedSiteId = sites.first().id
        }
    }

    // Load data when screen loads
    LaunchedEffect(Unit) {
        viewModel.loadUserAssignedSites()
        viewModel.loadUserAssignedBusinesses()
    }

    // Reset selections when businesses become unavailable
    LaunchedEffect(hasNoBusinesses) {
        if (hasNoBusinesses && selectedBusinessId != null) {
            selectedBusinessId = null
            selectedSiteId = null
        }
    }
    // Reset site selection when sites become unavailable
    LaunchedEffect(hasNoSites, selectedBusinessId) {
        if (hasNoSites && selectedSiteId != null) {
            selectedSiteId = null
        }
    }
    // Reset selections if selected business is no longer in available list
    LaunchedEffect(businesses, selectedBusinessId) {
        if (selectedBusinessId != null && !isLoading) {
            val isBusinessStillAvailable = businesses.any { it.id == selectedBusinessId }
            if (!isBusinessStillAvailable) {
                selectedBusinessId = null
                selectedSiteId = null
            }
        }
    }
    // Reset site selection if selected site is no longer in available list
    LaunchedEffect(sites, selectedSiteId) {
        if (selectedSiteId != null && !isLoading) {
            val isSiteStillAvailable = sites.any { it.id == selectedSiteId }
            if (!isSiteStillAvailable) {
                selectedSiteId = null
            }
        }
    }
    // Cargar sitios cuando cambia el business seleccionado
    LaunchedEffect(selectedBusinessId, currentUser) {
        val isAdmin = listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER).contains(currentUser?.role)
        if (selectedBusinessId != null && currentUser != null) {
            viewModel.loadSitesForBusinessWithRole(selectedBusinessId!!, isAdmin)
        }
    }
    
    // Handle setup completion
    LaunchedEffect(message, currentUser) {
        if (message?.contains("successfully") == true) {
            kotlinx.coroutines.delay(1500) // Show success message briefly
            
            // Use custom callback if provided, otherwise navigate based on user role
            if (onSetupComplete != null) {
                onSetupComplete()
            } else {
                // If showBackButton is true, it means user came from dashboard - just go back
                if (showBackButton) {
                    Log.d("UserPreferencesSetup", "Setup completed from dashboard, navigating back")
                    navController.popBackStack()
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
    }

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = showBackButton,
        topBarTitle = "Setup Your Preferences",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (hasNoBusinesses) Icons.Default.Info else Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (hasNoBusinesses) {
                                "No Business or Site Assigned"
                            } else if (currentUserPreferences != null) {
                                "Update Your Preferences"
                            } else {
                                "Welcome to Rig!"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (hasNoBusinesses) {
                                "No businesses or sites are currently assigned to your account. Contact your administrator to request access."
                            } else if (currentUserPreferences != null) {
                                "You can update your business and site preferences below. Your current selections are shown."
                            } else {
                                "To get started, please select your business and site. This helps us show you the right vehicles and information."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        // Contact Administrator button - only show when no businesses
                        if (hasNoBusinesses) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showContactAdminDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Contact Administrator")
                            }
                            
                            if (feedbackSubmitted) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✓ Request sent! Your administrator will be notified.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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
                                        Color(0xFF4CAF50).copy(alpha = 0.1f) // Light green background
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
                                        Color(0xFF2E7D32) // Dark green text
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
                            enabled = !isLoading && !hasNoBusinesses,
                            trailingIcon = {
                                IconButton(
                                    onClick = { showBusinessDropdown = true },
                                    enabled = !hasNoBusinesses
                                ) {
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
                            businesses.forEach { business ->
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
                                        selectedSiteId = null
                                        viewModel.loadSitesForBusinessWithRole(
                                            business.id,
                                            isAdmin = listOf(UserRole.ADMIN, UserRole.SUPERADMIN, UserRole.SYSTEM_OWNER).contains(currentUser?.role)
                                        )
                                    }
                                )
                            }
                            
                            if (businesses.isEmpty() && !isLoading) {
                                DropdownMenuItem(
                                    text = { Text("No businesses assigned") },
                                    onClick = { showBusinessDropdown = false },
                                    enabled = false
                                )
                            }
                        }
                        
                        Text(
                            text = if (hasNoBusinesses) {
                                "No businesses available - Contact administrator"
                            } else {
                                "Available businesses: ${businesses.size}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasNoBusinesses) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
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
                            enabled = !isLoading && selectedBusinessId != null && !hasNoSites,
                            trailingIcon = {
                                IconButton(
                                    onClick = { 
                                        if (selectedBusinessId != null && !hasNoSites) {
                                            showSiteDropdown = true 
                                        }
                                    },
                                    enabled = selectedBusinessId != null && !hasNoSites
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
                            sites.forEach { site ->
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
                            
                            if (sites.isEmpty() && !isLoading) {
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
                        } else if (hasNoSites) {
                            Text(
                                text = "No sites available for this business - Contact administrator",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            Text(
                                text = "Available sites: ${sites.size}",
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
                                    viewModel.createPreferencesWithBusinessAndSite(selectedBusinessId!!, selectedSiteId!!)
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
                            // Solo muestra el loader pequeño si NO hay overlay global
                            if (isLoading.not()) {
                                // Loader pequeño solo si no hay overlay
                                // (opcional: puedes quitarlo si prefieres solo overlay global)
                            }
                            Text(
                                text = if (isLoading) "Saving..." else if (currentUserPreferences != null) "Update Preferences" else "Complete Setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        if (!canSetup && !hasNoBusinesses) {
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
            }
            
            // Pull Refresh Indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            // 🔥 GLOBAL LOADING OVERLAY (siempre al final del Box, fuera de Card/Column)
            if (isLoading) {
                app.forku.presentation.common.components.LoadingOverlay(
                    message = "Guardando preferencias..."
                )
            }
        }
    }

    // Contact Administrator Dialog
    if (showContactAdminDialog) {
        ContactAdministratorDialog(
            onDismiss = { showContactAdminDialog = false },
            onSubmit = { feedback ->
                viewModel.submitBusinessAssignmentRequest(feedback)
                feedbackSubmitted = true
                showContactAdminDialog = false
            }
        )
    }

    // Reset feedback submitted state after 5 seconds
    LaunchedEffect(feedbackSubmitted) {
        if (feedbackSubmitted) {
            kotlinx.coroutines.delay(5000)
            feedbackSubmitted = false
        }
    }
}

@Composable
private fun ContactAdministratorDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var requestText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Request Business/Site Assignment",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Please describe which business and site you need access to:",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = requestText,
                    onValueChange = { requestText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Example: I need access to ABC Company and the Main Warehouse site...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Text(
                    text = "Your request will be sent to the administrators who can assign the appropriate business and site to your account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (requestText.isNotBlank()) {
                        onSubmit(requestText)
                    }
                },
                enabled = requestText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Request")
            }
        },
        modifier = Modifier.padding(16.dp)
    )
} 