package app.forku.domain.model.user

data class UserPreferences(
    val id: String? = null,
    val user: User? = null, // Complete user object instead of just ID
    val businessId: String? = null, // Default business
    val siteId: String? = null, // Default site
    val lastSelectedBusinessId: String? = null,
    val lastSelectedSiteId: String? = null,
    val theme: String = "system", // "light", "dark", "system"
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val isActive: Boolean = true,
    val isDirty: Boolean = true,
    val isNew: Boolean = true
) {
    /**
     * Get the user ID from the user object
     */
    fun getUserId(): String? {
        return user?.id
    }
    
    /**
     * Get the business ID to use (priority: lastSelected -> default)
     */
    fun getEffectiveBusinessId(): String? {
        return lastSelectedBusinessId ?: businessId
    }
    
    /**
     * Get the site ID to use (priority: lastSelected -> default)
     */
    fun getEffectiveSiteId(): String? {
        return lastSelectedSiteId ?: siteId
    }
    
    /**
     * Check if user has any business context
     */
    fun hasBusinessContext(): Boolean {
        return !businessId.isNullOrBlank() || !lastSelectedBusinessId.isNullOrBlank()
    }
    
    /**
     * Check if user has any site context
     */
    fun hasSiteContext(): Boolean {
        return !siteId.isNullOrBlank() || !lastSelectedSiteId.isNullOrBlank()
    }
} 