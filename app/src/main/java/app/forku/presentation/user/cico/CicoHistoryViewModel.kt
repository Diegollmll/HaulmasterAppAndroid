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
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CicoHistoryState())
    val state = _state.asStateFlow()

    init {
        //android.util.Log.d("appflow", "CicoHistoryViewModel init called")
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
                android.util.Log.d("CICO", "Loading history - isAdmin: $isAdmin, operatorId: $operatorId, page: $page")

                if (!append) {
                    _state.update { it.copy(
                        isAdmin = isAdmin
                    ) }
                    if (isAdmin) {
                        loadOperators()
                    }
                }

                // Get paginated sessions from API
                val sessions = try {
                    android.util.Log.d("CICO", "Fetching sessions page: $page")
                    cicoHistoryRepository.getSessionsHistory(page)
                } catch (e: Exception) {
                    android.util.Log.e("CICO", "Error loading sessions", e)
                    emptyList()
                }

                android.util.Log.d("CICO", "Fetched ${sessions.size} sessions")

                // Filter sessions based on operatorId or current user
                val filteredSessions = sessions.filter { session ->
                    if (operatorId != null) {
                        session.userId == operatorId
                    } else if (!isAdmin) {
                        session.userId == currentUser?.id
                    } else {
                        true // Admin sees all sessions when no operator selected
                    }
                }

                // Process sessions with vehicle and operator details
                val processedSessions = filteredSessions.mapNotNull { session ->
                    try {
                        val vehicle = vehicleRepository.getVehicle(session.vehicleId)
                        val operatorResult = loadOperatorWithRetry(session.userId)
                        
                        val operator = operatorResult.getOrNull()
                        if (operator == null) {
                            android.util.Log.w("CICO", "Could not load operator for session ${session.id}")
                            return@mapNotNull null
                        }
                        
                        CicoEntry(
                            id = session.id,
                            operatorId = session.userId,
                            vehicleName = vehicle.codename,
                            operatorName = "${operator.firstName} ${operator.lastName}",
                            date = getRelativeTimeSpanString(session.startTime),
                            checkInTime = getRelativeTimeSpanString(session.startTime),
                            checkOutTime = session.endTime?.let { getRelativeTimeSpanString(it) }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("CICO", "Error loading details for session ${session.id}", e)
                        null
                    }
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
    val isLoadingMore: Boolean = false
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
    val checkOutTime: String?
) 