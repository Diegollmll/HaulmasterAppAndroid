package app.forku.presentation.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.domain.model.Site
import app.forku.presentation.dashboard.Business
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserPreferences
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.site.SiteRepository
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.user.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.firstOrNull
import app.forku.data.mapper.toDomain

// Estado combinado para la pantalla
data class UserPreferencesSetupScreenState(
    val businesses: List<Business> = emptyList(),
    val sites: List<Site> = emptyList(),
    val currentUser: User? = null,
    val currentUserPreferences: UserPreferences? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class UserPreferencesSetupScreenViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val businessRepository: BusinessRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserPreferencesSetupScreenState())
    val state: StateFlow<UserPreferencesSetupScreenState> = _state.asStateFlow()

    val currentUser = MutableStateFlow<User?>(null)
    val currentUserPreferences = MutableStateFlow<UserPreferences?>(null)
    val sitesUiState = MutableStateFlow(UserPreferencesSetupScreenState().copy(sites = emptyList()))
    val businessesUiState = MutableStateFlow(UserPreferencesSetupScreenState().copy(businesses = emptyList()))
    val isLoading = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    init {
        loadCurrentUser()
        loadCurrentUserPreferences()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            currentUser.value = user
            _state.value = _state.value.copy(currentUser = user)
        }
    }

    fun loadCurrentUserPreferences() {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.getCurrentUserPreferences()
            currentUserPreferences.value = prefs
            _state.value = _state.value.copy(currentUserPreferences = prefs)
        }
    }

    fun loadUserAssignedBusinesses() {
        viewModelScope.launch {
            isLoading.value = true
            val user = currentUser.value ?: userRepository.getCurrentUser()
            val businessIds = userRepository.getCurrentUserAssignedBusinesses()
            val businesses = businessIds.mapNotNull { id ->
                try { businessRepository.getBusinessById(id) } catch (_: Exception) { null }
            }
            businessesUiState.value = businessesUiState.value.copy(businesses = businesses)
            _state.value = _state.value.copy(businesses = businesses)
            isLoading.value = false
        }
    }

    fun loadSitesForBusinessWithRole(businessId: String, isAdmin: Boolean) {
        viewModelScope.launch {
            isLoading.value = true
            val sites = if (isAdmin) {
                siteRepository.getSitesForBusiness(businessId)
                    .catch { }
                    .mapNotNull { it.getOrNull() }
                    .firstOrNull()
                    ?.map { it.toDomain() } ?: emptyList()
            } else {
                siteRepository.getUserAssignedSites()
                    .catch { }
                    .mapNotNull { it.getOrNull() }
                    .firstOrNull()
                    ?.filter { it.businessId == businessId }
                    ?.map { it.toDomain() } ?: emptyList()
            }
            sitesUiState.value = sitesUiState.value.copy(sites = sites)
            _state.value = _state.value.copy(sites = sites)
            isLoading.value = false
        }
    }

    fun loadUserAssignedSites() {
        viewModelScope.launch {
            isLoading.value = true
            val sites = siteRepository.getUserAssignedSites()
                .catch { }
                .mapNotNull { it.getOrNull() }
                .firstOrNull()
                ?.map { it.toDomain() } ?: emptyList()
            sitesUiState.value = sitesUiState.value.copy(sites = sites)
            _state.value = _state.value.copy(sites = sites)
            isLoading.value = false
        }
    }

    fun createPreferencesWithBusinessAndSite(businessId: String, siteId: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                userPreferencesRepository.createPreferencesWithBusinessAndSite(businessId, siteId)
                message.value = "Preferences saved successfully"
                loadCurrentUserPreferences()
            } catch (e: Exception) {
                message.value = "Error saving preferences: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun submitBusinessAssignmentRequest(feedback: String) {
        // Implementar lógica de feedback si aplica
        message.value = "Request sent to administrator"
    }
} 