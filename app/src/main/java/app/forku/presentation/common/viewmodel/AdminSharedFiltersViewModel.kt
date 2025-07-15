package app.forku.presentation.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import app.forku.data.storage.FilterStorage
import android.util.Log
import kotlinx.coroutines.flow.firstOrNull
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.user.UserPreferencesRepository

@HiltViewModel
class AdminSharedFiltersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val filterStorage: FilterStorage,
    private val userPreferencesRepository: UserPreferencesRepository // <-- Inyectar
) : ViewModel() {
    
    private val _filterBusinessId = MutableStateFlow<String?>(null)
    val filterBusinessId: StateFlow<String?> = _filterBusinessId.asStateFlow()

    private val _filterSiteId = MutableStateFlow<String?>(null)
    val filterSiteId: StateFlow<String?> = _filterSiteId.asStateFlow()
    
    // Special flag to track "All Sites" selection
    private val _isAllSitesSelected = MutableStateFlow(false)
    val isAllSitesSelected: StateFlow<Boolean> = _isAllSitesSelected.asStateFlow()

    private var filtersInitialized = false

    override fun onCleared() {
        super.onCleared()
        // No specific actions needed here for now, as the ViewModel is HiltViewModel
    }

    init {
        viewModelScope.launch {
            // Solo inicializar si los StateFlow están vacíos (no hay selección previa)
            val alreadySelected = _filterBusinessId.value != null || _filterSiteId.value != null || _isAllSitesSelected.value
            if (!alreadySelected && !filtersInitialized) {
                Log.d("AdminSharedFilters", "[FLOW] INIT: Lanzando inicialización de filtros tras login")
                // 1. Leer de FilterStorage
                val storedBusinessId = filterStorage.getBusinessId().firstOrNull()
                val storedSiteId = filterStorage.getSiteId().firstOrNull()
                val storedAllSites = filterStorage.getAllSitesSelected().firstOrNull() ?: false

                if (!storedBusinessId.isNullOrBlank()) {
                    setBusinessId(storedBusinessId)
                    Log.d("AdminSharedFilters", "[FLOW] INIT: businessId from FilterStorage: $storedBusinessId")
                } else {
                    // 2. Si no hay, leer de UserPreferencesRepository
                    val prefsBusinessId = userPreferencesRepository.getEffectiveBusinessId()
                    if (!prefsBusinessId.isNullOrBlank()) {
                        setBusinessId(prefsBusinessId)
                        filterStorage.saveBusinessId(prefsBusinessId)
                        Log.d("AdminSharedFilters", "[FLOW] INIT: businessId from UserPreferences: $prefsBusinessId")
                    } else {
                        Log.w("AdminSharedFilters", "[FLOW] INIT: No businessId found in FilterStorage or UserPreferences")
                    }
                }

                if (!storedSiteId.isNullOrBlank()) {
                    setSiteId(storedSiteId)
                    Log.d("AdminSharedFilters", "[FLOW] INIT: siteId from FilterStorage: $storedSiteId")
                } else {
                    val prefsSiteId = userPreferencesRepository.getEffectiveSiteId()
                    if (!prefsSiteId.isNullOrBlank()) {
                        setSiteId(prefsSiteId)
                        filterStorage.saveSiteId(prefsSiteId)
                        Log.d("AdminSharedFilters", "[FLOW] INIT: siteId from UserPreferences: $prefsSiteId")
                    } else {
                        Log.w("AdminSharedFilters", "[FLOW] INIT: No siteId found in FilterStorage or UserPreferences")
                    }
                }

                setAllSitesSelected(storedAllSites)
                Log.d("AdminSharedFilters", "[FLOW] INIT: allSitesSelected from FilterStorage: $storedAllSites")

                filtersInitialized = true
                Log.d("AdminSharedFilters", "[FLOW] INIT: Filtros inicializados correctamente")
            } else {
                Log.d("AdminSharedFilters", "[FLOW] INIT: Ya hay filtros seleccionados, no se sobrescriben")
            }
        }
    }

    private fun loadFilterState() {
        viewModelScope.launch {
            try {
                // Load filter business ID
                filterStorage.getBusinessId().collect { businessId ->
                    Log.d("AdminSharedFilters", "Loaded filter businessId: $businessId")
                    _filterBusinessId.value = businessId
                }
            } catch (e: Exception) {
                Log.e("AdminSharedFilters", "Error loading filter business ID", e)
            }
        }
        
        viewModelScope.launch {
            try {
                // Load filter site ID and "All Sites" flag
                filterStorage.getSiteId().collect { siteId ->
                    Log.d("AdminSharedFilters", "Loaded filter siteId: $siteId")
                    _filterSiteId.value = siteId
                }
            } catch (e: Exception) {
                Log.e("AdminSharedFilters", "Error loading filter site ID", e)
            }
        }
        
        viewModelScope.launch {
            try {
                // Load "All Sites" selection flag
                filterStorage.getAllSitesSelected().collect { isAllSites ->
                    Log.d("AdminSharedFilters", "Loaded isAllSitesSelected: $isAllSites")
                    _isAllSitesSelected.value = isAllSites
                }
            } catch (e: Exception) {
                Log.e("AdminSharedFilters", "Error loading All Sites flag", e)
            }
        }
    }

    fun setBusinessId(businessId: String?) {
        _filterBusinessId.value = businessId
        viewModelScope.launch { filterStorage.saveBusinessId(businessId) }
    }

    fun setSiteId(siteId: String?) {
        _filterSiteId.value = siteId
        viewModelScope.launch { filterStorage.saveSiteId(siteId) }
    }
    
    /**
     * Get the effective site ID for filtering
     * Returns null when "All Sites" is selected, actual siteId otherwise
     */
    fun getEffectiveSiteId(): String? {
        return if (_isAllSitesSelected.value) null else _filterSiteId.value
    }
    
    /**
     * Check if "All Sites" is currently selected
     */
    fun isAllSitesMode(): Boolean {
        return _isAllSitesSelected.value
    }
    
    /**
     * Clear all filter selections
     */
    fun clearFilters() {
        _filterBusinessId.value = null
        _filterSiteId.value = null
        _isAllSitesSelected.value = false
        viewModelScope.launch { filterStorage.clearFilters() }
    }

    fun setAllSitesSelected(allSites: Boolean) {
        _isAllSitesSelected.value = allSites
        viewModelScope.launch { filterStorage.saveAllSitesSelected(allSites) }
    }

    private suspend fun loadPersistedFilters() {
        Log.d("AdminSharedFilters", "[FLOW] loadPersistedFilters: Start loading from storage")
        filterStorage.getBusinessId().collect { 
            Log.d("AdminSharedFilters", "[FLOW] loadPersistedFilters: getBusinessId() -> $it")
            _filterBusinessId.value = it 
        }
        filterStorage.getSiteId().collect { 
            Log.d("AdminSharedFilters", "[FLOW] loadPersistedFilters: getSiteId() -> $it")
            _filterSiteId.value = it 
        }
        filterStorage.getAllSitesSelected().collect { 
            Log.d("AdminSharedFilters", "[FLOW] loadPersistedFilters: getAllSitesSelected() -> $it")
            _isAllSitesSelected.value = it 
        }
    }
} 