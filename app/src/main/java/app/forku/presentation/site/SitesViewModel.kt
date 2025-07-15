package app.forku.presentation.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.Site
import app.forku.domain.repository.site.SiteRepository
import app.forku.domain.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SitesUiState(
    val sites: List<Site> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDialog: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val selectedSite: Site? = null
)

@HiltViewModel
class SitesViewModel @Inject constructor(
    private val repository: SiteRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()

    /**
     * Load sites assigned to the current user
     */
    fun loadUserAssignedSites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                android.util.Log.d("SitesViewModel", "Starting to load user assigned sites...")
                
                // Get site IDs assigned to current user
                val assignedSiteIds = userRepository.getCurrentUserAssignedSites()
                android.util.Log.d("SitesViewModel", "User assigned site IDs: $assignedSiteIds")
                
                if (assignedSiteIds.isEmpty()) {
                    android.util.Log.d("SitesViewModel", "No sites assigned to user")
                    _uiState.update {
                        it.copy(
                            sites = emptyList(),
                            isLoading = false
                        )
                    }
                    return@launch
                }
                
                // Fetch details for each assigned site using async
                val sites = mutableListOf<Site>()
                assignedSiteIds.forEach { siteId ->
                    try {
                        android.util.Log.d("SitesViewModel", "Fetching site details for: $siteId")
                        repository.getSiteById(siteId).collect { result ->
                            result.fold(
                                onSuccess = { siteDto ->
                                    android.util.Log.d("SitesViewModel", "Successfully loaded site: ${siteDto.name}")
                                    sites.add(siteDto.toDomain())
                                },
                                onFailure = { e ->
                                    android.util.Log.w("SitesViewModel", "Failed to load site $siteId: ${e.message}")
                                }
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("SitesViewModel", "Exception loading site $siteId: ${e.message}")
                    }
                }
                
                android.util.Log.d("SitesViewModel", "Loaded ${sites.size} sites successfully")
                _uiState.update {
                    it.copy(
                        sites = sites,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("SitesViewModel", "Error loading user assigned sites", e)
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load assigned sites",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadSites(businessId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAllSites()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to load sites",
                            isLoading = false
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { sites ->
                            _uiState.update {
                                it.copy(
                                    sites = sites.map { it.toDomain() },
                                    isLoading = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to load sites",
                                    isLoading = false
                                )
                            }
                        }
                    )
                }
        }
    }

    /**
     * ✅ NEW: Load all sites for a specific business (for admin filtering)
     * This is different from loadUserAssignedSites which only shows user's assigned sites
     */
    fun loadSitesForBusiness(businessId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                android.util.Log.d("SitesViewModel", "Loading all sites for business: $businessId")
                
                // Load all sites for the specific business
                repository.getSitesForBusiness(businessId)
                    .catch { e ->
                        android.util.Log.e("SitesViewModel", "Error loading sites for business: $businessId", e)
                        _uiState.update {
                            it.copy(
                                error = e.message ?: "Failed to load sites for business",
                                isLoading = false
                            )
                        }
                    }
                    .collect { result ->
                        result.fold(
                            onSuccess = { sites ->
                                android.util.Log.d("SitesViewModel", "Loaded ${sites.size} sites for business $businessId")
                                _uiState.update {
                                    it.copy(
                                        sites = sites.map { it.toDomain() },
                                        isLoading = false
                                    )
                                }
                            },
                            onFailure = { e ->
                                android.util.Log.e("SitesViewModel", "Failed to load sites for business: $businessId", e)
                                _uiState.update {
                                    it.copy(
                                        error = e.message ?: "Failed to load sites for business",
                                        isLoading = false
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                android.util.Log.e("SitesViewModel", "Exception loading sites for business: $businessId", e)
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Exception loading sites for business",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Carga los sitios para un business según el rol del usuario.
     * Admin: todos los sitios del business.
     * Operator: solo los sitios asignados al usuario de ese business.
     */
    fun loadSitesForBusinessWithRole(businessId: String, isAdmin: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (isAdmin) {
                    // Admin: todos los sitios del business
                    repository.getSitesForBusiness(businessId)
                        .catch { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to load sites for business",
                                    isLoading = false
                                )
                            }
                        }
                        .collect { result ->
                            result.fold(
                                onSuccess = { sites ->
                                    _uiState.update {
                                        it.copy(
                                            sites = sites.map { it.toDomain() },
                                            isLoading = false
                                        )
                                    }
                                },
                                onFailure = { e ->
                                    _uiState.update {
                                        it.copy(
                                            error = e.message ?: "Failed to load sites for business",
                                            isLoading = false
                                        )
                                    }
                                }
                            )
                        }
                } else {
                    // Operator: solo sitios asignados al usuario de ese business
                    repository.getUserAssignedSites()
                        .catch { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to load assigned sites",
                                    isLoading = false
                                )
                            }
                        }
                        .collect { result ->
                            result.fold(
                                onSuccess = { sites ->
                                    val filtered = sites.map { it.toDomain() }.filter { it.businessId == businessId }
                                    _uiState.update {
                                        it.copy(
                                            sites = filtered,
                                            isLoading = false
                                        )
                                    }
                                },
                                onFailure = { e ->
                                    _uiState.update {
                                        it.copy(
                                            error = e.message ?: "Failed to load assigned sites",
                                            isLoading = false
                                        )
                                    }
                                }
                            )
                        }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Exception loading sites",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createSite(businessId: String, site: Site) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.saveSite(site.copy(businessId = businessId).toDto())
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to create site",
                            isLoading = false
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { createdSite ->
                            _uiState.update { currentState ->
                                currentState.copy(
                                    sites = currentState.sites + createdSite.toDomain(),
                                    isLoading = false,
                                    showDialog = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to create site",
                                    isLoading = false
                                )
                            }
                        }
                    )
                }
        }
    }

    fun updateSite(businessId: String, site: Site) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.saveSite(site.copy(businessId = businessId).toDto())
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to update site",
                            isLoading = false
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { updatedSite ->
                            _uiState.update { currentState ->
                                currentState.copy(
                                    sites = currentState.sites.map {
                                        if (it.id == updatedSite.toDomain().id) updatedSite.toDomain() else it
                                    },
                                    isLoading = false,
                                    showDialog = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to update site",
                                    isLoading = false
                                )
                            }
                        }
                    )
                }
        }
    }

    fun deleteSite(businessId: String, siteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.deleteSite(siteId)
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to delete site",
                            isLoading = false
                        )
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = {
                            _uiState.update { currentState ->
                                currentState.copy(
                                    sites = currentState.sites.filter { it.id != siteId },
                                    isLoading = false,
                                    showDeleteConfirmation = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to delete site",
                                    isLoading = false
                                )
                            }
                        }
                    )
                }
        }
    }

    fun showAddSiteDialog() {
        _uiState.update { 
            it.copy(
                showDialog = true,
                selectedSite = null
            )
        }
    }

    fun showEditSiteDialog(site: Site) {
        _uiState.update { 
            it.copy(
                showDialog = true,
                selectedSite = site
            )
        }
    }

    fun hideDialog() {
        _uiState.update { 
            it.copy(
                showDialog = false,
                selectedSite = null
            )
        }
    }

    fun showDeleteConfirmation(site: Site) {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = true,
                selectedSite = site
            )
        }
    }

    fun hideDeleteConfirmation() {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = false,
                selectedSite = null
            )
        }
    }
} 