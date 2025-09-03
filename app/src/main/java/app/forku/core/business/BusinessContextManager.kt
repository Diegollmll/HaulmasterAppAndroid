package app.forku.core.business

import android.content.Context
import android.util.Log
import app.forku.core.Constants
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.user.UserPreferencesRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext

data class BusinessContextState(
    val businessId: String? = null,
    val siteId: String? = null,
    val hasRealBusinessContext: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "business_context")

@Singleton
class BusinessContextManager @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) {
    private val _contextState = MutableStateFlow(BusinessContextState())
    val contextState: StateFlow<BusinessContextState> = _contextState.asStateFlow()

    private val businessIdKey = stringPreferencesKey("business_id")
    private val siteIdKey = stringPreferencesKey("site_id")

    /**
     * Get current business ID (cached or from repository)
     */
    suspend fun getCurrentBusinessId(): String? {
        val current = _contextState.value.businessId
        return if (current != null) {
            current
        } else {
            // Intenta cargar de DataStore
            val stored = context.dataStore.data.first()[businessIdKey]
            if (stored != null) {
                _contextState.value = _contextState.value.copy(businessId = stored)
                stored
            } else {
                // Don't automatically load business context - return null if not found
                // This allows the app to detect when user needs to go to SystemSettings
                val effectiveBusinessId = userPreferencesRepository.getEffectiveBusinessId()
                if (effectiveBusinessId != null) {
                    // Actualizar el estado interno para futuras llamadas
                    _contextState.value = _contextState.value.copy(businessId = effectiveBusinessId)
                }
                effectiveBusinessId
            }
        }
    }

    /**
     * Get current site ID (cached or from repository)
     */
    suspend fun getCurrentSiteId(): String? {
        val current = _contextState.value.siteId
        return if (current != null) {
            current
        } else {
            // Intenta cargar de DataStore
            val stored = context.dataStore.data.first()[siteIdKey]
            if (stored != null) {
                _contextState.value = _contextState.value.copy(siteId = stored)
                stored
            } else {
                // ✅ FIXED: Usar el mismo patrón que getCurrentBusinessId() - cargar desde user preferences
                val effectiveSiteId = userPreferencesRepository.getEffectiveSiteId()
                if (effectiveSiteId != null) {
                    // Actualizar el estado interno para futuras llamadas
                    _contextState.value = _contextState.value.copy(siteId = effectiveSiteId)
                }
                effectiveSiteId
            }
        }
    }

    /**
     * Load business context ONLY from user preferences - no fallbacks
     */
    suspend fun loadBusinessContext(): String? {
        return try {
            _contextState.value = _contextState.value.copy(isLoading = true, error = null)
            
            Log.d("BusinessContextManager", "🔍 loadBusinessContext called - fetching user preferences")
            
            // Load effective context from user preferences with detailed error handling
            val preferences = try {
                Log.d("BusinessContextManager", "🔍 About to call userPreferencesRepository.getCurrentUserPreferences()")
                val result = userPreferencesRepository.getCurrentUserPreferences()
                Log.d("BusinessContextManager", "🔍 getCurrentUserPreferences() returned: ${result != null}")
                result
            } catch (e: Exception) {
                Log.e("BusinessContextManager", "❌ Exception in getCurrentUserPreferences(): ${e.message}", e)
                null
            }
            
            Log.d("BusinessContextManager", "🔍 User preferences loading result:")
            Log.d("BusinessContextManager", "  - Preferences found: ${preferences != null}")
            
            if (preferences != null) {
                Log.d("BusinessContextManager", "  - Business ID: ${preferences.businessId}")
                Log.d("BusinessContextManager", "  - Site ID: ${preferences.siteId}")
                Log.d("BusinessContextManager", "  - Last Selected Business: ${preferences.lastSelectedBusinessId}")
                Log.d("BusinessContextManager", "  - Last Selected Site: ${preferences.lastSelectedSiteId}")
            } else {
                Log.w("BusinessContextManager", "⚠️ No preferences loaded - this may indicate:")
                Log.w("BusinessContextManager", "  1. User has no UserPreferencesId in backend")
                Log.w("BusinessContextManager", "  2. UserPreferences exist but API call failed")
                Log.w("BusinessContextManager", "  3. Network/authentication issue")
            }
            
            val businessId = preferences?.getEffectiveBusinessId()
            val siteId = preferences?.getEffectiveSiteId()
            
            Log.d("BusinessContextManager", "🔍 Effective context from preferences:")
            Log.d("BusinessContextManager", "  - Effective Business ID: $businessId")
            Log.d("BusinessContextManager", "  - Effective Site ID: $siteId")
            
            val hasRealContext = isValidGuid(businessId) && isValidGuid(siteId)
            
            if (!hasRealContext) {
                Log.d("BusinessContextManager", "❌ No valid user preferences found (need both BusinessId AND SiteId) - user needs to select in SystemSettings")
                if (preferences != null) {
                    Log.d("BusinessContextManager", "  NOTE: User has preferences but missing required fields")
                }
            } else {
                Log.d("BusinessContextManager", "✅ Valid business context found from preferences")
            }
            
            _contextState.value = _contextState.value.copy(
                businessId = if (hasRealContext) businessId else null,
                siteId = if (hasRealContext) siteId else null,
                hasRealBusinessContext = hasRealContext,
                isLoading = false,
                error = null
            )
            
            // Only persist if we have valid context
            if (hasRealContext) {
                persistBusinessId(businessId)
                persistSiteId(siteId)
            }
            
            if (hasRealContext) businessId else null
            
        } catch (e: Exception) {
            Log.e("BusinessContextManager", "Error loading business context: ${e.message}", e)
            
            _contextState.value = _contextState.value.copy(
                businessId = null,
                siteId = null,
                hasRealBusinessContext = false,
                isLoading = false,
                error = e.message
            )
            
            null
        }
    }

    /**
     * Refresh business context by re-fetching user with businesses
     */
    suspend fun refreshBusinessContext(): String? {
        return try {
            _contextState.value = _contextState.value.copy(isLoading = true, error = null)
            
            Log.d("BusinessContextManager", "Refreshing business context...")
            
            // Re-fetch user with businesses to update context
            val currentUserId = userRepository.getCurrentUserId()
            if (currentUserId != null) {
                userRepository.getUserWithBusinesses(currentUserId)
            }
            
            // Reload business context
            loadBusinessContext()
            
        } catch (e: Exception) {
            Log.e("BusinessContextManager", "Error refreshing business context: ${e.message}", e)
            
            // DON'T use Constants.BUSINESS_ID as fallback - return null
            _contextState.value = _contextState.value.copy(
                businessId = null,
                hasRealBusinessContext = false,
                isLoading = false,
                error = e.message
            )
            
            null
        }
    }

    /**
     * Clear business context
     */
    suspend fun clearBusinessContext() {
        _contextState.value = BusinessContextState()
        
        // Clear DataStore as well
        context.dataStore.edit { prefs ->
            prefs.remove(businessIdKey)
            prefs.remove(siteIdKey)
        }
        
        Log.d("BusinessContextManager", "Business context and DataStore cleared")
    }

    /**
     * Check if current context is real business or fallback
     */
    fun hasRealBusinessContext(): Boolean {
        return _contextState.value.hasRealBusinessContext
    }

    /**
     * Check if a GUID is valid (not null and not empty GUID)
     */
    private fun isValidGuid(guid: String?): Boolean {
        return guid != null && 
               guid.isNotBlank() && 
               guid != "00000000-0000-0000-0000-000000000000"
    }

    /**
     * Check if user has valid preferences (both BusinessId AND SiteId)
     */
    fun hasValidUserPreferences(): Boolean {
        val state = _contextState.value
        return isValidGuid(state.businessId) && 
               isValidGuid(state.siteId) && 
               state.hasRealBusinessContext
    }

    /**
     * Check if user needs to setup business/site preferences
     * Requires BOTH businessId AND siteId from user preferences
     */
    suspend fun userNeedsPreferencesSetup(): Boolean {
        val hasValidPrefs = hasValidUserPreferences()
        
        Log.d("BusinessContextManager", "Checking if user needs preferences setup:")
        Log.d("BusinessContextManager", "  Current Business ID: ${_contextState.value.businessId}")
        Log.d("BusinessContextManager", "  Current Site ID: ${_contextState.value.siteId}")
        Log.d("BusinessContextManager", "  Has Valid Preferences: $hasValidPrefs")
        
        return !hasValidPrefs
    }

    fun setCurrentBusinessId(businessId: String?) {
        _contextState.value = _contextState.value.copy(businessId = businessId)
        // Persistir en DataStore
        persistBusinessId(businessId)
        // Actualizar preferencias del usuario
        updateUserPreferencesAsync { 
            userPreferencesRepository.updateLastSelectedBusinessId(businessId ?: "")
        }
    }

    fun setCurrentSiteId(siteId: String?) {
        _contextState.value = _contextState.value.copy(siteId = siteId)
        // Persistir en DataStore
        persistSiteId(siteId)
        // Actualizar preferencias del usuario
        updateUserPreferencesAsync { 
            if (siteId != null) {
                userPreferencesRepository.updateLastSelectedSiteId(siteId)
            }
        }
    }

    /**
     * Set business as default preference
     */
    fun setDefaultBusinessId(businessId: String?) {
        updateUserPreferencesAsync { 
            if (businessId != null) {
                userPreferencesRepository.updateDefaultBusinessId(businessId)
            }
        }
    }

    /**
     * Set site as default preference
     */
    fun setDefaultSiteId(siteId: String?) {
        updateUserPreferencesAsync { 
            if (siteId != null) {
                userPreferencesRepository.updateDefaultSiteId(siteId)
            }
        }
    }

    private fun persistBusinessId(businessId: String?) {
        // Guardar en DataStore (no suspending porque se llama desde setCurrentBusinessId)
        GlobalScope.launch {
            context.dataStore.edit { prefs ->
                if (businessId != null) prefs[businessIdKey] = businessId else prefs.remove(businessIdKey)
            }
        }
    }

    private fun persistSiteId(siteId: String?) {
        GlobalScope.launch {
            context.dataStore.edit { prefs ->
                if (siteId != null) prefs[siteIdKey] = siteId else prefs.remove(siteIdKey)
            }
        }
    }

    private fun updateUserPreferencesAsync(updateAction: suspend () -> Unit) {
        GlobalScope.launch {
            try {
                updateAction()
                Log.d("BusinessContextManager", "User preferences updated successfully")
            } catch (e: Exception) {
                Log.w("BusinessContextManager", "Could not update user preferences (may not exist yet): ${e.message}")
                // Don't treat this as a critical error - preferences might not exist yet
                // The user will create them through SystemSettings when they select business/site
            }
        }
    }
} 