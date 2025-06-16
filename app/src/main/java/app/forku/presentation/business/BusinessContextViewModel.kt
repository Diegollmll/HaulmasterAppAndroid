package app.forku.presentation.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class BusinessContextState(
    val currentBusinessId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasBusinessContext: Boolean = false
)

@HiltViewModel
class BusinessContextViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BusinessContextState())
    val state: StateFlow<BusinessContextState> = _state

    init {
        loadBusinessContext()
    }

    private fun loadBusinessContext() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Get current business ID
                val businessId = userRepository.getCurrentUserBusinessId()
                
                if (businessId != null) {
                    Log.d("BusinessContextVM", "Found business context: $businessId")
                    
                    _state.update { 
                        it.copy(
                            currentBusinessId = businessId,
                            hasBusinessContext = true,
                            isLoading = false
                        )
                    }
                } else {
                    Log.d("BusinessContextVM", "No business context found, user needs business assignment")
                    _state.update { 
                        it.copy(
                            currentBusinessId = null,
                            hasBusinessContext = false,
                            isLoading = false,
                            error = "No business assigned to user"
                        )
                    }
                }
                
            } catch (e: Exception) {
                Log.e("BusinessContextVM", "Error loading business context: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load business context: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshBusinessContext() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                if (currentUserId != null) {
                    Log.d("BusinessContextVM", "Refreshing business context for user: $currentUserId")
                    
                    // Re-fetch user with businesses
                    userRepository.getUserWithBusinesses(currentUserId)
                    
                    // Reload the context
                    loadBusinessContext()
                } else {
                    Log.w("BusinessContextVM", "No current user ID found")
                }
            } catch (e: Exception) {
                Log.e("BusinessContextVM", "Error refreshing business context: ${e.message}", e)
                _state.update { 
                    it.copy(error = "Failed to refresh business context: ${e.message}")
                }
            }
        }
    }

    fun clearBusinessContext() {
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    currentBusinessId = null,
                    hasBusinessContext = false,
                    error = null
                )
            }
        }
    }
} 