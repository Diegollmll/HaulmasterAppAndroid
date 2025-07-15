package app.forku.presentation.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.core.business.BusinessContextManager
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.user.UserPreferencesRepository
import app.forku.presentation.common.components.BusinessContextUpdater
import app.forku.presentation.common.components.updateBusinessContext
import app.forku.presentation.common.components.updateSiteContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class AllChecklistViewModel @Inject constructor(
    private val checklistAnswerRepository: ChecklistAnswerRepository,
    override val businessContextManager: BusinessContextManager,
    private val userRepository: UserRepository, // <-- add this
    override val userPreferencesRepository: UserPreferencesRepository
    // ✅ REMOVED: private val siteRepository: SiteRepository - BusinessSiteFilters handles sites
) : ViewModel(), BusinessContextUpdater {

    private val _state = MutableStateFlow(AllChecklistState())
    val state = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // ✅ REMOVED: loadSitesForBusiness method - BusinessSiteFilters handles sites internally

    init {
        // ✅ REMOVED: No automatic loading - will be triggered by filters
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                // Handle error silently as this is not critical
            }
        }
    }

    fun loadNextPage() {
        if (_state.value.isLoadingMore || !_state.value.hasMoreItems) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            loadChecks(
                page = _state.value.currentPage + 1,
                append = true
            )
        }
    }

    fun loadChecks(
        page: Int = 1,
        append: Boolean = false
    ) {
        viewModelScope.launch {
            if (!append) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            
            try {
                // ✅ UPDATED: Use context-based loading for backward compatibility
                val answers = checklistAnswerRepository.getAllPaginated(page, _state.value.itemsPerPage)
                val checkStates = answers.map { answer ->
                        PreShiftCheckState(
                            id = answer.id,
                            vehicleId = answer.vehicleId,
                        vehicleCodename = answer.vehicleName,
                        operatorName = answer.operatorName,
                            status = answer.status.toString(),
                            lastCheckDateTime = answer.lastCheckDateTime.takeIf { it.isNotBlank() }
                        )
                }
                
                _state.update { currentState ->
                    val updatedChecks = if (append) {
                        currentState.checks + checkStates
                    } else {
                        checkStates
                    }
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        checks = updatedChecks,
                        currentPage = if (checkStates.isNotEmpty()) page else currentState.currentPage,
                        // If we received fewer items than requested, we've reached the end
                        hasMoreItems = checkStates.size == currentState.itemsPerPage
                    )
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "Failed to load checks: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * ✅ NEW: Load checks with temporary filters (VIEW_FILTER mode)
     * Does NOT change user's personal context/preferences
     * Used for admin filtering across different sites
     */
    fun loadChecksWithFilters(
        filterBusinessId: String?,
        filterSiteId: String?,
        page: Int = 1,
        append: Boolean = false
    ) {
        Log.d("AllChecklistViewModel", "appflow loadChecksWithFilters called with businessId=$filterBusinessId, siteId=$filterSiteId, page=$page, append=$append A::")
        viewModelScope.launch {
            Log.d("AllChecklistViewModel", "[VM] loadChecksWithFilters called with businessId=$filterBusinessId, siteId=$filterSiteId, page=$page, append=$append")
            if (!append) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            
            try {
                Log.d("AllChecklistViewModel", "[loadChecksWithFilters] === 🚀 ADMIN CHECKLIST LOAD ===")
                Log.d("AllChecklistViewModel", "[loadChecksWithFilters] INPUTS: filterBusinessId=$filterBusinessId, filterSiteId=$filterSiteId, page=$page, append=$append")
                
                if (filterBusinessId.isNullOrBlank()) {
                    Log.w("AllChecklistViewModel", "[loadChecksWithFilters] ❌ No business ID available")
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = "No business context available"
                        )
                    }
                    return@launch
                }
                
                // ✅ Use explicit filter parameters ONLY, never user context
                val businessId = filterBusinessId
                val siteId = filterSiteId // null = "All Sites"
                
                Log.d("AllChecklistViewModel", "[loadChecksWithFilters] Calling repo.getAllWithFilters with businessId=$businessId, siteId=$siteId, page=$page")
                val answers = checklistAnswerRepository.getAllWithFilters(
                    businessId = businessId,
                    siteId = siteId,
                    page = page,
                    pageSize = _state.value.itemsPerPage
                )
                
                val checkStates = answers.map { answer ->
                    PreShiftCheckState(
                        id = answer.id,
                        vehicleId = answer.vehicleId,
                        vehicleCodename = answer.vehicleName,
                        operatorName = answer.operatorName,
                        status = answer.status.toString(),
                        lastCheckDateTime = answer.lastCheckDateTime.takeIf { it.isNotBlank() }
                    )
                }
                
                Log.d("AllChecklistViewModel", "[loadChecksWithFilters] ✅ Received ${checkStates.size} checks with filters")
                
                _state.update { currentState ->
                    val updatedChecks = if (append) {
                        currentState.checks + checkStates
                    } else {
                        checkStates
                    }
                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = null,
                        checks = updatedChecks,
                        currentPage = if (checkStates.isNotEmpty()) page else currentState.currentPage,
                        // If we received fewer items than requested, we've reached the end
                        hasMoreItems = checkStates.size == currentState.itemsPerPage
                    )
                }
                
                Log.d("AllChecklistViewModel", "[loadChecksWithFilters] === ✅ ADMIN CHECKLIST LOAD COMPLETED ===")
                
            } catch (e: Exception) {
                Log.e("AllChecklistViewModel", "[loadChecksWithFilters] ❌ Error: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "Failed to load checks: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Implementation of BusinessContextUpdater interface
     * Reloads checks when context changes
     */
    override fun reloadData() {
        loadChecks()
    }
} 