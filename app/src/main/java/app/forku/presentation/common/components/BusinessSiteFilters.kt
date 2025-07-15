package app.forku.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.forku.presentation.business.BusinessesViewModel
import app.forku.presentation.site.SitesViewModel
import app.forku.core.business.BusinessContextManager
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log

/**
 * Centralized and reusable Business and Site filters component
 * Supports two modes:
 * - CONTEXT_UPDATE: Changes user personal preferences (permanent)
 * - VIEW_FILTER: Temporary filtering for data visualization (admins)
 */

enum class BusinessSiteFilterMode {
    CONTEXT_UPDATE, // Changes user context (permanent preferences)
    VIEW_FILTER     // Temporary filters for data visualization
}

@Composable
fun BusinessSiteFilters(
    modifier: Modifier = Modifier,
    mode: BusinessSiteFilterMode = BusinessSiteFilterMode.CONTEXT_UPDATE, // ✅ NEW: Specify mode
    onBusinessChanged: ((String) -> Unit)? = null,
    onSiteChanged: ((String) -> Unit)? = null,
    showTitle: Boolean = true,
    title: String = if (mode == BusinessSiteFilterMode.VIEW_FILTER) "View Filters" else "Context Filters",
    showBusinessFilter: Boolean = true,
    isCollapsible: Boolean = true,
    initiallyExpanded: Boolean = false,
    currentUserRole: app.forku.domain.model.user.UserRole? = null, // ✅ User role to determine filter behavior
    selectedBusinessId: String? = null, // ✅ For VIEW_FILTER mode: Current filter selection
    selectedSiteId: String? = null, // ✅ For VIEW_FILTER mode: Current filter selection
    isAllSitesSelected: Boolean = false, // ✅ NEW: Flag to indicate "All Sites" is selected
    businessesViewModel: BusinessesViewModel = hiltViewModel(),
    sitesViewModel: SitesViewModel = hiltViewModel(),
    businessContextManager: BusinessContextManager = hiltViewModel<BusinessContextAwareViewModel>().businessContextManager,
    adminSharedFiltersViewModel: app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel = hiltViewModel()
) {
    // ✅ Get business context state for fallback when selectedBusinessId/selectedSiteId not provided
    val businessContextState by businessContextManager.contextState.collectAsState()
    
    // ✅ Get centralized admin filter state
    val adminFilterBusinessId by adminSharedFiltersViewModel.filterBusinessId.collectAsState()
    val adminFilterSiteId by adminSharedFiltersViewModel.filterSiteId.collectAsState()
    val adminIsAllSitesSelected by adminSharedFiltersViewModel.isAllSitesSelected.collectAsState()
    
    // ✅ Use effective selections based on mode
    val effectiveBusinessId = when (mode) {
        BusinessSiteFilterMode.VIEW_FILTER -> adminFilterBusinessId ?: selectedBusinessId ?: businessContextState.businessId
        BusinessSiteFilterMode.CONTEXT_UPDATE -> selectedBusinessId ?: businessContextState.businessId
    }
    
    // ✅ Use effective site selection based on mode
    val effectiveSiteId = when (mode) {
        BusinessSiteFilterMode.VIEW_FILTER -> {
            when {
                adminIsAllSitesSelected -> null // All Sites selected in admin filters
                adminFilterSiteId != null -> adminFilterSiteId // Use admin filter site
                selectedSiteId != null -> selectedSiteId // Fallback to explicit selection
                else -> businessContextState.siteId // Fallback to user context
            }
        }
        BusinessSiteFilterMode.CONTEXT_UPDATE -> {
            when {
                isAllSitesSelected -> null // All Sites selected in context mode
                selectedSiteId != null -> selectedSiteId // Explicit site selection
                else -> businessContextState.siteId // User context fallback
            }
        }
    }
    // States
    val businessesUiState by businessesViewModel.uiState.collectAsState()
    val sitesUiState by sitesViewModel.uiState.collectAsState()
    
    // Dropdown states
    var showBusinessDropdown by remember { mutableStateOf(false) }
    var showSiteDropdown by remember { mutableStateOf(false) }
    
    // Expansion state for collapsible filters
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    
    // ✅ Determine if user is admin (can see all sites in business)
    val isAdmin = currentUserRole in listOf(
        app.forku.domain.model.user.UserRole.ADMIN,
        app.forku.domain.model.user.UserRole.SUPERADMIN,
        app.forku.domain.model.user.UserRole.SYSTEM_OWNER
    )
    
    // ✅ "All Sites" only available in VIEW_FILTER mode for admins
    val showAllSitesOption = mode == BusinessSiteFilterMode.VIEW_FILTER && isAdmin
    
    // ✅ Use effective selections (filter state OR user context fallback)
    val selectedBusiness = businessesUiState.businesses.find { it.id == effectiveBusinessId }
    
    // ✅ IMPROVED: More robust site selection logic
    val selectedSite = when {
        effectiveSiteId == null -> null // No site selected/All sites mode
        effectiveSiteId == "ALL_SITES" -> null // Special "All Sites" case
        else -> {
            // Try to find the site in loaded sites first
            val foundSite = sitesUiState.sites.find { it.id == effectiveSiteId }
            
            if (foundSite != null) {
                foundSite
            } else if (sitesUiState.isLoading) {
                // Sites are still loading, don't show error yet
                null
            } else {
                // Site not found in loaded sites - this might be a preselection issue
                // For now, return null but log the issue
                Log.w("BusinessSiteFilters", "⚠️ Site '$effectiveSiteId' not found in loaded sites")
                null
            }
        }
    }
    
    // ✅ DEBUG: Enhanced logging for site selection troubleshooting
    LaunchedEffect(effectiveSiteId, sitesUiState.sites.size, selectedSite) {
        Log.d("BusinessSiteFilters", "=== SITE SELECTION DEBUG ===")
        Log.d("BusinessSiteFilters", "effectiveSiteId: $effectiveSiteId")
        Log.d("BusinessSiteFilters", "sitesUiState.sites.size: ${sitesUiState.sites.size}")
        Log.d("BusinessSiteFilters", "selectedSite found: ${selectedSite?.name} (${selectedSite?.id})")
        
        if (effectiveSiteId != null && selectedSite == null && sitesUiState.sites.isNotEmpty()) {
            Log.w("BusinessSiteFilters", "🚨 PRESELECTION ISSUE: effectiveSiteId '$effectiveSiteId' not found in available sites:")
            sitesUiState.sites.forEach { site ->
                Log.w("BusinessSiteFilters", "  Available site: ${site.name} (${site.id})")
            }
        }
        
        if (effectiveSiteId != null && sitesUiState.sites.isEmpty()) {
            Log.d("BusinessSiteFilters", "⏳ Sites not loaded yet, will retry when sites load")
        }
        
        Log.d("BusinessSiteFilters", "=============================")
    }
    
    // Load data when component mounts
    LaunchedEffect(Unit) {
        businessesViewModel.loadUserAssignedBusinesses()
    }
    
    // ✅ Load sites based on mode, user role and effective business
    LaunchedEffect(effectiveBusinessId, isAdmin, mode) {
        Log.d("BusinessSiteFilters", "=== SITE LOADING LOGIC ===")
        Log.d("BusinessSiteFilters", "effectiveBusinessId: $effectiveBusinessId")
        Log.d("BusinessSiteFilters", "isAdmin: $isAdmin")
        Log.d("BusinessSiteFilters", "mode: $mode")
        
        when {
            mode == BusinessSiteFilterMode.VIEW_FILTER && isAdmin && effectiveBusinessId != null -> {
                // ✅ VIEW_FILTER + Admin: Load ALL sites for the business (for filtering)
                Log.d("BusinessSiteFilters", "🔧 VIEW_FILTER: Admin loading all sites for business: $effectiveBusinessId")
                sitesViewModel.loadSitesForBusiness(effectiveBusinessId!!)
            }
            mode == BusinessSiteFilterMode.VIEW_FILTER && isAdmin && effectiveBusinessId == null -> {
                // ✅ VIEW_FILTER + Admin BUT no business ID - wait for business context
                Log.d("BusinessSiteFilters", "⏸️ VIEW_FILTER: Admin waiting for business context to load sites")
            }
            mode == BusinessSiteFilterMode.CONTEXT_UPDATE || !isAdmin -> {
                // ✅ CONTEXT_UPDATE mode OR Operator: Load only their assigned sites
                Log.d("BusinessSiteFilters", "👤 CONTEXT_UPDATE or Operator: Loading user assigned sites only")
                sitesViewModel.loadUserAssignedSites()
            }
            else -> {
                // ✅ Fallback: load assigned sites
                Log.d("BusinessSiteFilters", "⏳ Fallback: Loading assigned sites")
                sitesViewModel.loadUserAssignedSites()
            }
        }
        Log.d("BusinessSiteFilters", "===========================")
    }
    
    // ✅ ADDITIONAL: Ensure current user's site is always available
    LaunchedEffect(businessContextState.siteId, sitesUiState.sites.size) {
        val userSiteId = businessContextState.siteId
        if (userSiteId != null && sitesUiState.sites.isNotEmpty()) {
            val userSiteExists = sitesUiState.sites.any { it.id == userSiteId }
            if (!userSiteExists) {
                Log.w("BusinessSiteFilters", "🚨 User's current site '$userSiteId' not found in loaded sites")
                Log.w("BusinessSiteFilters", "Available sites: ${sitesUiState.sites.map { "${it.name} (${it.id})" }}")
                
                // In this case, we might need to load user assigned sites as fallback
                if (mode == BusinessSiteFilterMode.VIEW_FILTER && isAdmin) {
                    Log.d("BusinessSiteFilters", "🔄 Admin mode: Falling back to user assigned sites to include current site")
                    sitesViewModel.loadUserAssignedSites()
                }
            }
        }
    }
    
    // ✅ DEBUG: Enhanced logging for troubleshooting
    LaunchedEffect(selectedBusinessId, businessContextState.businessId, mode, isAdmin) {
        Log.d("BusinessSiteFilters", "=== BUSINESS SITE FILTERS DEBUG ===")
        Log.d("BusinessSiteFilters", "Mode: $mode")
        Log.d("BusinessSiteFilters", "IsAdmin: $isAdmin")
        Log.d("BusinessSiteFilters", "selectedBusinessId (explicit): $selectedBusinessId")
        Log.d("BusinessSiteFilters", "businessContextState.businessId (fallback): ${businessContextState.businessId}")
        Log.d("BusinessSiteFilters", "effectiveBusinessId (final): $effectiveBusinessId")
        Log.d("BusinessSiteFilters", "selectedSiteId (explicit): $selectedSiteId")
        Log.d("BusinessSiteFilters", "businessContextState.siteId (fallback): ${businessContextState.siteId}")
        Log.d("BusinessSiteFilters", "effectiveSiteId (final): $effectiveSiteId")
        Log.d("BusinessSiteFilters", "showAllSitesOption: $showAllSitesOption")
        Log.d("BusinessSiteFilters", "====================================")
    }
    
    // ✅ NEW: When collapsible and collapsed, show only a small floating button
    if (isCollapsible && !isExpanded) {
        // Collapsed state - only show small filter button
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(
                onClick = { isExpanded = true },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    Icons.Default.FilterList,
                    contentDescription = "Show filters",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    } else {
        // Expanded state or non-collapsible - show full card
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // ✅ Header with title and collapse button (if collapsible)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showTitle) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        if (isCollapsible) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { isExpanded = false }
                            ) {
                                Icon(
                                    Icons.Default.ExpandLess,
                                    contentDescription = "Hide filters",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Filters row
                if (showBusinessFilter) {
                    // Show both Business and Site filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Business Filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedBusiness?.name ?: "Select Business",
                                onValueChange = {},
                                label = { Text("Business") },
                                readOnly = true,
                                enabled = !businessesUiState.isLoading,
                                trailingIcon = {
                                    IconButton(onClick = { showBusinessDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Business")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (selectedBusiness != null) {
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
                                onDismissRequest = { showBusinessDropdown = false }
                            ) {
                                businessesUiState.businesses.forEach { business ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(business.name)
                                                Text(
                                                    text = business.id.take(8) + "...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showBusinessDropdown = false
                                            // ✅ Update centralized filters when in VIEW_FILTER mode
                                            if (mode == BusinessSiteFilterMode.VIEW_FILTER) {
                                                adminSharedFiltersViewModel.setBusinessId(business.id)
                                            }
                                            onBusinessChanged?.invoke(business.id)
                                        }
                                    )
                                }
                                
                                if (businessesUiState.businesses.isEmpty() && !businessesUiState.isLoading) {
                                    DropdownMenuItem(
                                        text = { Text("No businesses available") },
                                        onClick = { showBusinessDropdown = false },
                                        enabled = false
                                    )
                                }
                            }
                        }
                        
                        // Site Filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = when {
                                    isAllSitesSelected && showAllSitesOption -> "All Sites" // ✅ Explicit All Sites selection
                                    selectedSite != null -> selectedSite.name
                                    effectiveSiteId != null && sitesUiState.isLoading -> "Loading..."
                                    effectiveSiteId != null && selectedSite == null -> "Selected Site (${effectiveSiteId.take(8)}...)"
                                    else -> "Select Site"
                                },
                                onValueChange = {},
                                label = { Text("Site") },
                                readOnly = true,
                                enabled = !sitesUiState.isLoading && selectedBusiness != null,
                                trailingIcon = {
                                    IconButton(
                                        onClick = { 
                                            if (selectedBusiness != null) {
                                                showSiteDropdown = true 
                                            }
                                        },
                                        enabled = selectedBusiness != null
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Site")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (selectedSite != null || (isAllSitesSelected && showAllSitesOption) || effectiveSiteId != null) {
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
                                onDismissRequest = { showSiteDropdown = false }
                            ) {
                                // ✅ Add "All Sites" option only in VIEW_FILTER mode for admins
                                if (showAllSitesOption) {
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(
                                                    text = "All Sites",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "View data from all sites in business",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showSiteDropdown = false
                                            // ✅ Update centralized filters when in VIEW_FILTER mode
                                            if (mode == BusinessSiteFilterMode.VIEW_FILTER) {
                                                adminSharedFiltersViewModel.setSiteId(null) // null = All Sites
                                                adminSharedFiltersViewModel.setAllSitesSelected(true)
                                            }
                                            onSiteChanged?.invoke("ALL_SITES") // ✅ Special value for All Sites
                                        }
                                    )
                                    
                                    // Add divider between "All Sites" and specific sites
                                    if (sitesUiState.sites.isNotEmpty()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                                
                                sitesUiState.sites.forEach { site ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(site.name)
                                                Text(
                                                    text = site.id.take(8) + "...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showSiteDropdown = false
                                            if (mode == BusinessSiteFilterMode.VIEW_FILTER) {
                                                adminSharedFiltersViewModel.setSiteId(site.id)
                                                adminSharedFiltersViewModel.setAllSitesSelected(false)
                                            }
                                            onSiteChanged?.invoke(site.id)
                                        }
                                    )
                                }
                                
                                if (sitesUiState.sites.isEmpty() && !sitesUiState.isLoading) {
                                    DropdownMenuItem(
                                        text = { Text("No sites available") },
                                        onClick = { showSiteDropdown = false },
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Show only Site filter (Business is hidden/fixed)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = when {
                                    isAllSitesSelected && showAllSitesOption -> "All Sites" // ✅ Explicit All Sites selection
                                    selectedSite != null -> selectedSite.name
                                    effectiveSiteId != null && sitesUiState.isLoading -> "Loading..."
                                    effectiveSiteId != null && selectedSite == null -> "Selected Site (${effectiveSiteId.take(8)}...)"
                                    else -> "Select Site"
                                },
                                onValueChange = {},
                                label = { Text("Site") },
                                readOnly = true,
                                enabled = !sitesUiState.isLoading,
                                trailingIcon = {
                                    IconButton(onClick = { showSiteDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Site")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (selectedSite != null || (isAllSitesSelected && showAllSitesOption) || effectiveSiteId != null) {
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
                                onDismissRequest = { showSiteDropdown = false }
                            ) {
                                // ✅ Add "All Sites" option only in VIEW_FILTER mode for admins
                                if (showAllSitesOption) {
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(
                                                    text = "All Sites",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "View data from all sites in business",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showSiteDropdown = false
                                            // ✅ Update centralized filters when in VIEW_FILTER mode
                                            if (mode == BusinessSiteFilterMode.VIEW_FILTER) {
                                                adminSharedFiltersViewModel.setSiteId(null) // null = All Sites
                                                adminSharedFiltersViewModel.setAllSitesSelected(true)
                                            }
                                            onSiteChanged?.invoke("ALL_SITES") // ✅ Special value for All Sites
                                        }
                                    )
                                    
                                    // Add divider between "All Sites" and specific sites
                                    if (sitesUiState.sites.isNotEmpty()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                                
                                sitesUiState.sites.forEach { site ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(site.name)
                                                Text(
                                                    text = site.id.take(8) + "...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showSiteDropdown = false
                                            if (mode == BusinessSiteFilterMode.VIEW_FILTER) {
                                                adminSharedFiltersViewModel.setSiteId(site.id)
                                                adminSharedFiltersViewModel.setAllSitesSelected(false)
                                            }
                                            onSiteChanged?.invoke(site.id)
                                        }
                                    )
                                }
                                
                                if (sitesUiState.sites.isEmpty() && !sitesUiState.isLoading) {
                                    DropdownMenuItem(
                                        text = { Text("No sites available") },
                                        onClick = { showSiteDropdown = false },
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Context information
                if (selectedBusiness != null || selectedSite != null || (isAllSitesSelected && showAllSitesOption)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildString {
                            append("Showing data for: ")
                            if (showBusinessFilter && selectedBusiness != null) {
                                append(selectedBusiness.name)
                            } else if (!showBusinessFilter && selectedBusiness != null) {
                                // When business filter is hidden, still show the business name but indicate it's fixed
                                append(selectedBusiness.name)
                            }
                            
                            // ✅ NEW: Handle "All Sites" case for admins
                            if (selectedBusiness != null && (selectedSite != null || (isAllSitesSelected && showAllSitesOption))) {
                                append(" • ")
                            }
                            
                            when {
                                selectedSite != null -> append(selectedSite.name)
                                isAllSitesSelected && showAllSitesOption -> append("All Sites")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Helper ViewModel interface to access BusinessContextManager
 * This allows the component to work with any ViewModel that implements this interface
 */
interface BusinessContextAware {
    val businessContextManager: BusinessContextManager
}

/**
 * Default implementation for screens that don't have their own ViewModel with BusinessContextManager
 */
@HiltViewModel
class BusinessContextAwareViewModel @Inject constructor(
    override val businessContextManager: BusinessContextManager
) : ViewModel(), BusinessContextAware 