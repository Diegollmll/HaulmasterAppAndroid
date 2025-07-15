package app.forku.presentation.system

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.core.business.BusinessContextManager
import app.forku.domain.repository.user.UserPreferencesRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.feedback.SubmitFeedbackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SystemSettingsViewModel @Inject constructor(
    private val businessContextManager: BusinessContextManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    
    private val _currentUser = MutableStateFlow<app.forku.domain.model.user.User?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Current user preferences
    private val _currentUserPreferences = MutableStateFlow<app.forku.domain.model.user.UserPreferences?>(null)
    val currentUserPreferences = _currentUserPreferences.asStateFlow()

    // Temporary storage for selections before creating preferences
    private var tempSelectedBusinessId: String? = null
    private var tempSelectedSiteId: String? = null

    companion object {
        private const val TAG = "SystemSettingsViewModel"
    }
    
    init {
        loadCurrentUser()
        loadCurrentUserPreferences()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                var user = userRepository.getCurrentUser()
                
                // If user is null, try to reload from token
                if (user == null) {
                    Log.w(TAG, "⚠️ Current user is NULL - attempting to reload from authentication token")
                    try {
                        // Try to get the current user ID from token or other sources
                        val userId = userRepository.getCurrentUserId()
                        Log.d(TAG, "Direct user ID from repository: $userId")
                        
                        if (userId != null) {
                            Log.d(TAG, "Attempting to reload user by ID: $userId")
                            user = userRepository.getUserById(userId)
                            if (user != null) {
                                Log.d(TAG, "✅ Successfully reloaded user from ID")
                                // Update the current user in AuthDataStore to prevent future null issues
                                try {
                                    userRepository.updateCurrentUser(user)
                                    Log.d(TAG, "✅ Updated current user in AuthDataStore")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to update current user in AuthDataStore", e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to reload user from token", e)
                    }
                }
                
                _currentUser.value = user
                Log.d(TAG, "Current user loaded: ${user?.username} (${user?.role})")
                Log.d(TAG, "Current user ID: ${user?.id}")
                Log.d(TAG, "Current user email: ${user?.email}")
                
                // Additional debugging
                if (user == null) {
                    Log.w(TAG, "⚠️ Current user is STILL NULL after reload attempt - this will cause issues with assigned sites/businesses")
                    Log.w(TAG, "⚠️ User may need to re-login or there's a session management issue")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading current user", e)
            }
        }
    }

    private fun loadCurrentUserPreferences() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 SystemSettingsViewModel: Loading current user preferences...")
                val preferences = userPreferencesRepository.getCurrentUserPreferences()
                Log.d(TAG, "🔍 SystemSettingsViewModel: getCurrentUserPreferences returned: ${preferences != null}")
                
                _currentUserPreferences.value = preferences
                
                if (preferences != null) {
                    Log.d(TAG, "✅ SystemSettingsViewModel: Current user preferences loaded successfully:")
                    Log.d(TAG, "  Preferences ID: ${preferences.id}")
                    Log.d(TAG, "  User ID: ${preferences.user?.id}")
                    Log.d(TAG, "  Primary Business ID: ${preferences.businessId}")
                    Log.d(TAG, "  Primary Site ID: ${preferences.siteId}")
                    Log.d(TAG, "  Last Selected Business: ${preferences.lastSelectedBusinessId}")
                    Log.d(TAG, "  Last Selected Site: ${preferences.lastSelectedSiteId}")
                    Log.d(TAG, "  Effective Business ID: ${preferences.getEffectiveBusinessId()}")
                    Log.d(TAG, "  Effective Site ID: ${preferences.getEffectiveSiteId()}")
                } else {
                    Log.d(TAG, "❌ SystemSettingsViewModel: No current user preferences found - user needs setup")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ SystemSettingsViewModel: Error loading current user preferences", e)
                Log.e(TAG, "Error details: ${e.message}")
                Log.e(TAG, "Error cause: ${e.cause}")
                _currentUserPreferences.value = null
            }
        }
    }

    /**
     * Refresh current user preferences (call after creating/updating preferences)
     */
    fun refreshCurrentUserPreferences() {
        loadCurrentUserPreferences()
    }

    /**
     * Select business - store temporarily until site is also selected, or update existing preferences
     */
    fun selectBusiness(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "=== SELECTING BUSINESS ===")
                Log.d(TAG, "Business ID: $businessId")
                
                // Check if user already has preferences
                val currentPrefs = userPreferencesRepository.getCurrentUserPreferences()
                
                if (currentPrefs != null) {
                    // User has existing preferences, update them directly
                    Log.d(TAG, "User has existing preferences, updating business")
                    businessContextManager.setCurrentBusinessId(businessId)
                    refreshCurrentUserPreferences() // Refresh to update UI
                    _message.value = "Business updated and saved"
                } else {
                    // User has no preferences yet, store temporarily
                    Log.d(TAG, "No existing preferences, storing business temporarily")
                    tempSelectedBusinessId = businessId
                    
                    // Try to auto-select site if there's only one available
                    tryAutoSelectSiteIfOnlyOne()
                }
                
                Log.d(TAG, "✅ Business selection completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error selecting business", e)
                _message.value = "Failed to save business selection: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== BUSINESS SELECTION FINISHED ===")
            }
        }
    }
    
    /**
     * Try to auto-select site if there's only one available
     */
    private suspend fun tryAutoSelectSiteIfOnlyOne() {
        // This would need access to sites data - we'll implement this in the Screen level
        _message.value = "Business selected. Please select a site to complete setup."
    }

    /**
     * Select site - create preferences if this is the first time, or update existing ones
     */
    fun selectSite(siteId: String?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "=== SELECTING SITE ===")
                Log.d(TAG, "Site ID: $siteId")
                
                // Check if user already has preferences
                val currentPrefs = userPreferencesRepository.getCurrentUserPreferences()
                
                if (currentPrefs != null) {
                    // User has existing preferences, update them directly
                    Log.d(TAG, "User has existing preferences, updating site")
                    businessContextManager.setCurrentSiteId(siteId)
                    refreshCurrentUserPreferences() // Refresh to update UI
                    _message.value = if (siteId != null) "Site selected and saved" else "All sites selected"
                } else {
                    // User has no preferences yet, need to create them with both business and site
                    if (tempSelectedBusinessId != null && siteId != null) {
                        Log.d(TAG, "Creating new preferences with business: $tempSelectedBusinessId and site: $siteId")
                        
                        // Create preferences with both business and site
                        userPreferencesRepository.createPreferencesWithBusinessAndSite(tempSelectedBusinessId!!, siteId)
                        
                        // ✅ FIXED: Refresh business context to load the newly created preferences
                        Log.d(TAG, "Refreshing business context to load new preferences...")
                        businessContextManager.refreshBusinessContext()
                        
                        // Also explicitly set the context values to ensure immediate update
                        businessContextManager.setCurrentBusinessId(tempSelectedBusinessId!!)
                        businessContextManager.setCurrentSiteId(siteId)
                        
                        // Clear temporary storage
                        tempSelectedBusinessId = null
                        tempSelectedSiteId = null
                        
                        _message.value = "Business and site preferences created successfully!"
                    } else if (tempSelectedBusinessId == null) {
                        _message.value = "Please select a business first"
                    } else {
                        _message.value = "Please select a specific site (not 'All Sites') for initial setup"
                    }
                }
                
                Log.d(TAG, "✅ Site selection completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error selecting site", e)
                _message.value = "Failed to save site selection: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== SITE SELECTION FINISHED ===")
            }
        }
    }

    /**
     * Set default business and save preference immediately
     */
    fun setDefaultBusiness(businessId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "=== SETTING DEFAULT BUSINESS ===")
                Log.d(TAG, "Business ID: $businessId")
                
                // Update business context (this will handle the preferences update internally)
                businessContextManager.setDefaultBusinessId(businessId)
                Log.d(TAG, "✅ Default business context and preferences updated")
                
                _message.value = "Default business set and saved"
                Log.d(TAG, "✅ Default business setting completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error setting default business", e)
                _message.value = "Failed to save default business: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== DEFAULT BUSINESS SETTING FINISHED ===")
            }
        }
    }

    /**
     * Set default site and save preference immediately
     */
    fun setDefaultSite(siteId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "=== SETTING DEFAULT SITE ===")
                Log.d(TAG, "Site ID: $siteId")
                
                // Update site context (this will handle the preferences update internally)
                businessContextManager.setDefaultSiteId(siteId)
                Log.d(TAG, "✅ Default site context and preferences updated")
                
                _message.value = "Default site set and saved"
                Log.d(TAG, "✅ Default site setting completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error setting default site", e)
                _message.value = "Failed to save default site: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== DEFAULT SITE SETTING FINISHED ===")
            }
        }
    }

    /**
     * Create preferences with both business and site (matching working API structure)
     */
    fun createPreferencesWithBusinessAndSite(businessId: String, siteId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "=== CREATING PREFERENCES WITH BUSINESS AND SITE ===")
                Log.d(TAG, "Business ID: $businessId")
                Log.d(TAG, "Site ID: $siteId")
                
                // Create preferences with both values (like working Postman example)
                userPreferencesRepository.createPreferencesWithBusinessAndSite(businessId, siteId)
                
                // ✅ FIXED: Refresh business context to load the newly created preferences
                Log.d(TAG, "Refreshing business context to load new preferences...")
                businessContextManager.refreshBusinessContext()
                
                // ✅ FIXED: Refresh current user preferences to update UI state
                Log.d(TAG, "Refreshing current user preferences to update UI...")
                refreshCurrentUserPreferences()
                
                // Also explicitly set the context values to ensure immediate update
                businessContextManager.setCurrentBusinessId(businessId)
                businessContextManager.setCurrentSiteId(siteId)
                
                Log.d(TAG, "✅ Preferences created and context updated")
                
                _message.value = "Business and site preferences created successfully"
                Log.d(TAG, "✅ Preferences creation completed successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error creating preferences with business and site", e)
                _message.value = "Failed to create preferences: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== PREFERENCES CREATION FINISHED ===")
            }
        }
    }

    /**
     * Get temporarily selected business ID (for UI display)
     */
    fun getTempSelectedBusinessId(): String? = tempSelectedBusinessId
    
    /**
     * Get temporarily selected site ID (for UI display)  
     */
    fun getTempSelectedSiteId(): String? = tempSelectedSiteId
    
    /**
     * Check if user has temporary selections pending
     */
    fun hasTempSelections(): Boolean = tempSelectedBusinessId != null || tempSelectedSiteId != null
    
    /**
     * Clear temporary selections
     */
    fun clearTempSelections() {
        tempSelectedBusinessId = null
        tempSelectedSiteId = null
        _message.value = "Selections cleared. Please start over."
    }
    
    /**
     * Auto-select site if there's only one available (called from Screen)
     */
    fun autoSelectSiteIfOnlyOne(availableSites: List<app.forku.domain.model.Site>) {
        if (tempSelectedBusinessId != null && availableSites.size == 1) {
            Log.d(TAG, "Auto-selecting the only available site: ${availableSites.first().name}")
            selectSite(availableSites.first().id)
        }
    }
    
    /**
     * Clear message
     */
    fun clearMessage() {
        _message.value = null
    }
    
    /**
     * Submit business assignment request using feedback system
     */
    fun submitBusinessAssignmentRequest(requestDescription: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== SUBMITTING BUSINESS ASSIGNMENT REQUEST ===")
                Log.d(TAG, "Request: $requestDescription")
                
                // Use feedback system with special message format for business assignment requests
                val feedbackMessage = "BUSINESS ASSIGNMENT REQUEST: $requestDescription"
                
                submitFeedbackUseCase(
                    rating = 3, // Neutral rating for requests
                    comment = feedbackMessage,
                    canContactMe = true // Always allow contact for assignment requests
                ).onSuccess {
                    Log.d(TAG, "✅ Business assignment request submitted successfully")
                    _message.value = "Request sent! Administrator will be notified."
                }.onFailure { error ->
                    Log.e(TAG, "❌ Error submitting business assignment request", error)
                    _message.value = "Failed to send request: ${error.message}"
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error submitting business assignment request", e)
                _message.value = "Failed to send request: ${e.message}"
            }
        }
    }
} 