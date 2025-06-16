package app.forku.presentation.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.business.BusinessRepository
import app.forku.presentation.dashboard.Business
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class BusinessesUiState(
    val businesses: List<Business> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BusinessesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BusinessesUiState())
    val uiState: StateFlow<BusinessesUiState> = _uiState.asStateFlow()

    fun loadUserAssignedBusinesses() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                Log.d("BusinessesViewModel", "Loading user assigned businesses...")

                // Get business IDs assigned to current user
                val businessIds = userRepository.getCurrentUserAssignedBusinesses()
                Log.d("BusinessesViewModel", "Found ${businessIds.size} assigned business IDs: $businessIds")

                if (businessIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        businesses = emptyList(),
                        isLoading = false
                    )
                    return@launch
                }

                // Fetch business details for each ID
                val businesses = mutableListOf<Business>()
                businessIds.forEach { businessId ->
                    try {
                        Log.d("BusinessesViewModel", "Fetching business details for ID: $businessId")
                        val business = businessRepository.getBusinessById(businessId)
                        businesses.add(business)
                        Log.d("BusinessesViewModel", "Successfully fetched business: ${business.name}")
                    } catch (e: Exception) {
                        Log.e("BusinessesViewModel", "Error fetching business $businessId: ${e.message}", e)
                    }
                }

                Log.d("BusinessesViewModel", "Successfully loaded ${businesses.size} businesses")
                _uiState.value = _uiState.value.copy(
                    businesses = businesses,
                    isLoading = false
                )

            } catch (e: Exception) {
                Log.e("BusinessesViewModel", "Error loading user assigned businesses", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading businesses: ${e.message}"
                )
            }
        }
    }
} 