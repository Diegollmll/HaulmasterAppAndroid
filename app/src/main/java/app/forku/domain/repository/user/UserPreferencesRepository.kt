package app.forku.domain.repository.user

import app.forku.domain.model.user.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    
    /**
     * Get current user's preferences
     */
    suspend fun getCurrentUserPreferences(): UserPreferences?
    
    /**
     * Get user preferences by user ID
     */
    suspend fun getUserPreferences(userId: String): UserPreferences?
    
    /**
     * Save or update user preferences
     */
    suspend fun saveUserPreferences(preferences: UserPreferences): UserPreferences
    
    /**
     * Update default business for current user
     */
    suspend fun updateDefaultBusiness(businessId: String?): UserPreferences
    
    /**
     * Update default site for current user
     */
    suspend fun updateDefaultSite(siteId: String?): UserPreferences
    
    /**
     * Update last selected business (for session persistence)
     */
    suspend fun updateLastSelectedBusiness(businessId: String?): UserPreferences
    
    /**
     * Update last selected site (for session persistence)
     */
    suspend fun updateLastSelectedSite(siteId: String?): UserPreferences
    
    /**
     * Observe current user preferences changes
     */
    fun observeCurrentUserPreferences(): Flow<UserPreferences?>
    
    /**
     * Clear all preferences (logout scenario)
     */
    suspend fun clearPreferences()
    
    /**
     * Get effective business ID (last selected or default)
     */
    suspend fun getEffectiveBusinessId(): String?
    
    /**
     * Get effective site ID (last selected or default)
     */
    suspend fun getEffectiveSiteId(): String?

    /**
     * Check if user needs to configure preferences (redirect to SystemSettings)
     */
    suspend fun userNeedsPreferencesSetup(): Boolean

    /**
     * Update default business ID
     */
    suspend fun updateDefaultBusinessId(businessId: String)

    /**
     * Update default site ID
     */
    suspend fun updateDefaultSiteId(siteId: String)

    /**
     * Update last selected business ID
     */
    suspend fun updateLastSelectedBusinessId(businessId: String)

    /**
     * Update last selected site ID
     */
    suspend fun updateLastSelectedSiteId(siteId: String)

    /**
     * Create preferences with both BusinessId and SiteId (matching working API structure)
     */
    suspend fun createPreferencesWithBusinessAndSite(businessId: String, siteId: String): UserPreferences

    /**
     * Check if a vehicle belongs to the user's assigned business and site
     * @param vehicleBusinessId The business ID of the vehicle
     * @param vehicleSiteId The site ID of the vehicle
     * @return true if the vehicle belongs to the user's business and site, false otherwise
     */
    suspend fun isVehicleInUserContext(vehicleBusinessId: String, vehicleSiteId: String): Boolean
} 