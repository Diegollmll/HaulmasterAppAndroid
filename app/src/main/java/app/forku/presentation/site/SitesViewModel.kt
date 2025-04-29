package app.forku.presentation.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.Site
import app.forku.domain.repository.site.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val repository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()

    fun loadSites(businessId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val allSites = repository.getAllSites().map { it.toDomain() }
                val sites = allSites.filter { it.businessId == businessId }
                _uiState.update {
                    it.copy(
                        sites = sites,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to load sites",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createSite(businessId: String, site: Site) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val createdSite = repository.saveSite(site.copy(businessId = businessId).toDto()).toDomain()
                _uiState.update { currentState ->
                    currentState.copy(
                        sites = currentState.sites + createdSite,
                        isLoading = false,
                        showDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to create site",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSite(businessId: String, site: Site) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val updatedSite = repository.saveSite(site.copy(businessId = businessId).toDto()).toDomain()
                _uiState.update { currentState ->
                    currentState.copy(
                        sites = currentState.sites.map {
                            if (it.id == updatedSite.id) updatedSite else it
                        },
                        isLoading = false,
                        showDialog = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to update site",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteSite(businessId: String, siteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.deleteSite(siteId)
                _uiState.update { currentState ->
                    currentState.copy(
                        sites = currentState.sites.filter { it.id != siteId },
                        isLoading = false,
                        showDeleteConfirmation = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "Failed to delete site",
                        isLoading = false
                    )
                }
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