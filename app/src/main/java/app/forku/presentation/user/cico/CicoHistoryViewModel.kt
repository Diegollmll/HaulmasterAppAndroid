package app.forku.presentation.user.cico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.core.Constants
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.cico.CicoHistoryRepository

import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.forku.presentation.common.utils.getRelativeTimeSpanString
import app.forku.domain.repository.user.UserRepository
import kotlinx.coroutines.delay

@HiltViewModel
class CicoHistoryViewModel @Inject constructor(
    private val cicoHistoryRepository: CicoHistoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CicoHistoryState())
    val state = _state.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        android.util.Log.d("appflow", "CicoHistoryViewModel init called: state.value.selectedOperatorId ${state.value.selectedOperatorId}")
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                _currentUser.value = user
            } catch (e: Exception) {
                android.util.Log.e("CICO", "Error loading current user", e)
            }
        }
        //loadCicoHistory()
    }

    private suspend fun loadOperatorWithRetry(userId: String, retryCount: Int = 3): Result<User?> {
        repeat(retryCount) { attempt ->
            try {
                if (attempt > 0) {
                    delay(attempt * 1000L) // Exponential backoff
                }
                return Result.success(userRepository.getUserById(userId))
            } catch (e: Exception) {
                if (attempt == retryCount - 1) {
                    return Result.failure(e)
                }
                android.util.Log.w("CICO", "Retry attempt ${attempt + 1} for user $userId failed", e)
            }
        }
        return Result.failure(Exception("Failed after $retryCount attempts"))
    }

    private fun loadOperators() {
        viewModelScope.launch {
            try {
                val users = userRepository.getAllUsers()
                val operators = users.map { Operator(it.id, "${it.firstName} ${it.lastName}") }
                _state.update { it.copy(operators = operators) }
            } catch (e: Exception) {
                android.util.Log.e("CICO", "Error loading operators", e)
                _state.update { 
                    it.copy(error = "Failed to load operators. Please try again.")
                }
            }
        }
    }

    fun setSelectedOperator(operatorId: String?) {
        android.util.Log.d("appflow", "CicoHistoryViewModel setSelectedOperator called")
        android.util.Log.d("CICO", "Setting selected operator: $operatorId")
        _state.update { it.copy(
            selectedOperatorId = operatorId,
            currentPage = 1,
            cicoHistory = emptyList(),
            filteredHistory = emptyList(),
            hasMoreItems = true // Reset to allow loading first page
        ) }
        viewModelScope.launch {
            android.util.Log.d("CICO", "Loading history for operator: $operatorId")
            loadCicoHistory(operatorId = operatorId)
        }
    }

    private fun filterHistory() {
        // This function is no longer needed as we'll always fetch fresh data
    }

    fun loadNextPage() {
        android.util.Log.d("appflow", "CicoHistoryViewModel loadNextPage called")
        if (_state.value.isLoadingMore || !_state.value.hasMoreItems) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            loadCicoHistory(
                operatorId = _state.value.selectedOperatorId,
                page = _state.value.currentPage + 1,
                append = true
            )
        }
    }

    fun loadCicoHistory(
        operatorId: String? = null,
        page: Int = 1,
        append: Boolean = false
    ) {
        android.util.Log.d("CICO", "loadCicoHistory called - operatorId: $operatorId, page: $page, append: $append")
        
        // Prevent duplicate calls with same parameters
        val currentState = _state.value

        viewModelScope.launch {
            if (!append) {
                _state.update { it.copy(isLoading = true, error = null) }
            }
            
            try {
                val currentUser = userRepository.getCurrentUser()
                val isAdmin = currentUser?.role == UserRole.ADMIN
                android.util.Log.d("CICO", """
                    Loading history with:
                    - isAdmin: $isAdmin
                    - operatorId: $operatorId
                    - currentUser.id: ${currentUser?.id}
                    - selectedOperatorId: ${_state.value.selectedOperatorId}
                    - page: $page
                """.trimIndent())

                if (!append) {
                    _state.update { it.copy(
                        isAdmin = isAdmin,
                        currentUserId = currentUser?.id
                    ) }
                    if (isAdmin) {
                        loadOperators()
                    }
                }

                // Get paginated sessions from API
                val sessions = try {
                    android.util.Log.d("CICO", "Determining which API to call...")
                    
                    // If viewing from profile (source == "profile"), always use getCurrentUserSessionsHistory
                    if (operatorId == currentUser?.id) {
                        android.util.Log.d("CICO", "Loading current user sessions (from profile)")
                        cicoHistoryRepository.getCurrentUserSessionsHistory(page)
                    }
                    // If admin viewing all operators
                    else if (isAdmin && operatorId == null && _state.value.selectedOperatorId == null) {
                        android.util.Log.d("CICO", "Loading all sessions (admin view)")
                        cicoHistoryRepository.getSessionsHistory(page)
                    }
                    // If viewing specific operator
                    else if (operatorId != null || _state.value.selectedOperatorId != null) {
                        val targetUserId = operatorId ?: _state.value.selectedOperatorId
                        android.util.Log.d("CICO", "Loading sessions for specific operator: $targetUserId")
                        cicoHistoryRepository.getOperatorSessionsHistory(targetUserId!!, page)
                    }
                    // Default case: load current user's sessions
                    else {
                        android.util.Log.d("CICO", "Loading current user sessions (default case)")
                        cicoHistoryRepository.getCurrentUserSessionsHistory(page)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CICO", "Error loading sessions", e)
                    emptyList()
                }

                android.util.Log.d("CICO", "Fetched ${sessions.size} sessions")

                // Process sessions with included vehicle and operator details (optimized)
                val processedSessions = sessions.map { session ->
                    android.util.Log.d("CICO", "Processing session ${session.id}:")
                    android.util.Log.d("CICO", "  - UserId: ${session.userId}")
                    android.util.Log.d("CICO", "  - Operator: '${session.operatorName}'")
                    android.util.Log.d("CICO", "  - Vehicle: '${session.vehicleName}'")
                    android.util.Log.d("CICO", "  - StartTime: ${session.startTime}")

                    CicoEntry(
                        id = session.id,
                        operatorId = session.userId,
                        vehicleName = session.vehicleName,
                        operatorName = session.operatorName,
                        date = getRelativeTimeSpanString(session.startTime),
                        checkInTime = session.startTime,
                        checkOutTime = session.endTime,
                        duration = session.durationMinutes?.let { minutes ->
                            when {
                                minutes < 60 -> "$minutes min"
                                else -> "${minutes / 60}h ${minutes % 60}m"
                            }
                        }
                    )
                }

                android.util.Log.d("appflow", "Processed sessions: ${processedSessions.size}")

                // Update state with new sessions
                _state.update { currentState ->
                    val newHistory = if (append) {
                        currentState.cicoHistory + processedSessions
                    } else {
                        processedSessions
                    }

                    currentState.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        cicoHistory = newHistory,
                        filteredHistory = newHistory,
                        currentPage = if (processedSessions.isNotEmpty()) page else currentState.currentPage,
                        hasMoreItems = sessions.size >= currentState.itemsPerPage,
                        error = if (!append && processedSessions.isEmpty()) "No history found" else null
                    )
                }

                // If we filtered out all items but there might be more, load next page
                if (processedSessions.isEmpty() && sessions.isNotEmpty()) {
                    loadNextPage()
                }

            } catch (e: Exception) {
                android.util.Log.e("CICO", "Error in loadCicoHistory", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = "Failed to load history. Please try again."
                    )
                }
            }
        }
    }

    fun setDropdownExpanded(expanded: Boolean) {
        _state.update { it.copy(dropdownExpanded = expanded) }
    }

    fun clearState() {
        _state.update { 
            CicoHistoryState() // Reset to initial state
        }
    }

    // ✅ New methods for business and site filtering
    fun setBusinessFilter(businessId: String?) {
        android.util.Log.d("CICO", "Setting business filter: $businessId")
        _state.update { it.copy(selectedBusinessId = businessId) }
        // Reload history with new filters
        loadCicoHistoryWithFilters()
    }

    fun setSiteFilter(siteId: String?) {
        android.util.Log.d("CICO", "Setting site filter: $siteId")
        _state.update { it.copy(selectedSiteId = siteId) }
        // Reload history with new filters
        loadCicoHistoryWithFilters()
    }



    private fun loadCicoHistoryWithFilters() {
        val currentState = _state.value
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                android.util.Log.d("CICO", "Loading history with filters: businessId=${currentState.selectedBusinessId}, siteId=${currentState.selectedSiteId}, operatorId=${currentState.selectedOperatorId}")
                
                // Use the new filtered method
                val sessions = cicoHistoryRepository.getSessionsHistoryWithFilters(
                    page = 1, // Reset to first page when filtering
                    businessId = currentState.selectedBusinessId,
                    siteId = currentState.selectedSiteId,
                    operatorId = currentState.selectedOperatorId
                )

                android.util.Log.d("CICO", "Fetched ${sessions.size} filtered sessions")

                // Process sessions
                val processedSessions = sessions.map { session ->
                    CicoEntry(
                        id = session.id,
                        operatorId = session.userId,
                        vehicleName = session.vehicleName,
                        operatorName = session.operatorName,
                        date = getRelativeTimeSpanString(session.startTime),
                        checkInTime = session.startTime,
                        checkOutTime = session.endTime,
                        duration = session.durationMinutes?.let { minutes ->
                            when {
                                minutes < 60 -> "$minutes min"
                                else -> "${minutes / 60}h ${minutes % 60}m"
                            }
                        }
                    )
                }

                _state.update { 
                    it.copy(
                        isLoading = false,
                        cicoHistory = processedSessions,
                        filteredHistory = processedSessions,
                        currentPage = 1,
                        hasMoreItems = sessions.size >= it.itemsPerPage,
                        error = if (processedSessions.isEmpty()) "No history found with current filters" else null
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("CICO", "Error loading filtered history", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load filtered history. Please try again."
                    )
                }
            }
        }
    }
}

data class CicoHistoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cicoHistory: List<CicoEntry> = emptyList(),
    val filteredHistory: List<CicoEntry> = emptyList(),
    val isAdmin: Boolean = false,
    val operators: List<Operator> = emptyList(),
    val selectedOperatorId: String? = null,
    val dropdownExpanded: Boolean = false,
    val currentPage: Int = 1,
    val itemsPerPage: Int = 10,
    val hasMoreItems: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentUserId: String? = null,
    // ✅ New business and site filter states for admin
    val selectedBusinessId: String? = null,
    val selectedSiteId: String? = null,
    val currentUserRole: UserRole? = null
)

data class Operator(
    val id: String,
    val name: String
)

data class CicoEntry(
    val id: String,
    val operatorId: String,
    val vehicleName: String,
    val operatorName: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val duration: String?
) 