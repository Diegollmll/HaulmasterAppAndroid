package app.forku.data.repository.user

import android.util.Log
import app.forku.data.api.UserPreferencesApi
import app.forku.data.api.UserApi
import app.forku.data.api.dto.user.UserDto
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.data.mapper.getBusinessName
import app.forku.data.mapper.getSiteName
import app.forku.data.mapper.getUserName
import app.forku.domain.model.user.UserPreferences
import app.forku.domain.repository.user.UserPreferencesRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.core.auth.HeaderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val api: UserPreferencesApi,
    private val userApi: UserApi,
    private val userRepository: UserRepository,
    private val headerManager: HeaderManager
) : UserPreferencesRepository {

    private val _currentPreferences = MutableStateFlow<UserPreferences?>(null)
    
    private val gson = com.google.gson.Gson()
    
    companion object {
        private const val TAG = "UserPreferencesRepo"
    }

    override suspend fun getCurrentUserPreferences(): UserPreferences? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting current user preferences via UserRepository include")
            
            // Use UserRepository to get user with preferences included
            val userRepositoryImpl = userRepository as app.forku.data.repository.user.UserRepositoryImpl
            val (user, preferences) = userRepositoryImpl.getCurrentUserWithPreferences()
            
            if (user == null) {
                Log.w(TAG, "No current user found")
                return@withContext null
            }
            
            if (preferences == null) {
                Log.d(TAG, "No preferences found for user ${user.id}")
                return@withContext null // Don't create automatically, let individual methods handle it
            }
            
            Log.d(TAG, "Successfully loaded preferences for user ${user.id}")
            Log.d(TAG, "  Business ID: ${preferences.businessId}")
            Log.d(TAG, "  Site ID: ${preferences.siteId}")
            Log.d(TAG, "  Last Selected Business: ${preferences.lastSelectedBusinessId}")
            Log.d(TAG, "  Last Selected Site: ${preferences.lastSelectedSiteId}")
            Log.d(TAG, "  Effective context: businessId=${preferences.getEffectiveBusinessId()}, siteId=${preferences.getEffectiveSiteId()}")
            
            _currentPreferences.value = preferences
            preferences
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user preferences", e)
            null
        }
    }

    override suspend fun getUserPreferences(userId: String): UserPreferences? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting preferences for user: $userId")
            
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val filter = "GOUserId == Guid.Parse(\"$userId\")"
            val response = api.getAllUserPreferences(
                csrfToken = csrfToken,
                cookie = cookie,
                filter = filter
            )
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to get user preferences: ${response.code()}")
                return@withContext null
            }
            
            val preferencesDto = response.body()?.firstOrNull()
            preferencesDto?.toDomain()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user preferences", e)
            null
        }
    }

    override suspend fun saveUserPreferences(preferences: UserPreferences): UserPreferences = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Saving user preferences for user: ${preferences.getUserId()}")
            
            val saveDto = preferences.toDto()
            val entityJson = gson.toJson(saveDto)
            
            Log.d(TAG, "Sending UserPreferences JSON: $entityJson")
            
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.saveUserPreferences(
                entity = entityJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to save user preferences: ${response.code()}")
                Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                throw Exception("Failed to save user preferences: ${response.code()}")
            }
            
            val savedPreferences = response.body()?.toDomain()
                ?: throw Exception("Saved preferences response is null")
            
            // 🔄 RESTORED: Update user's UserPreferencesId after successful preferences save
            // This ensures the relationship is established even if backend doesn't handle it automatically
            try {
                Log.d(TAG, "🔄 Attempting to update user's UserPreferencesId: ${savedPreferences.id}")
                val updateResult = updateUserPreferencesId(savedPreferences.id)
                if (updateResult) {
                    Log.d(TAG, "✅ Successfully updated user's UserPreferencesId")
                } else {
                    Log.w(TAG, "⚠️ Failed to update user's UserPreferencesId, but preferences were saved")
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error updating user's UserPreferencesId (non-critical): ${e.message}")
                // Don't throw - preferences were saved successfully, user update is secondary
            }
            
            _currentPreferences.value = savedPreferences
            Log.d(TAG, "User preferences saved successfully")
            
            savedPreferences
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user preferences", e)
            throw e
        }
    }

    /**
     * Updates the user's UserPreferencesId to establish the relationship
     * @param userPreferencesId The ID of the saved UserPreferences
     * @return true if successful, false otherwise
     */
    private suspend fun updateUserPreferencesId(userPreferencesId: String?): Boolean {
        return try {
            if (userPreferencesId == null) {
                Log.w(TAG, "Cannot update user: userPreferencesId is null")
                return false
            }
            
            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                Log.w(TAG, "Cannot update user: No current user found")
                return false
            }
            
            Log.d(TAG, "Updating user ${currentUser.id} with UserPreferencesId: $userPreferencesId")
            
            // ⚠️ CRITICAL FIX: Get current user data to preserve password
            val currentUserResponse = userApi.getUser(currentUser.id)
            val currentUserData = currentUserResponse.body()
            val currentPassword = currentUserData?.password
            
            Log.d(TAG, "Password preservation check:")
            Log.d(TAG, "  API response successful: ${currentUserResponse.isSuccessful}")
            Log.d(TAG, "  Current user data found: ${currentUserData != null}")
            Log.d(TAG, "  Password found: ${!currentPassword.isNullOrBlank()}")
            Log.d(TAG, "  Password length: ${currentPassword?.length ?: 0}")
            
            // Create updated user DTO with UserPreferencesId
            val updatedUserDto = UserDto(
                id = currentUser.id,
                email = currentUser.email,
                username = currentUser.username,
                firstName = currentUser.firstName,
                lastName = currentUser.lastName,
                fullName = "${currentUser.firstName} ${currentUser.lastName}".trim(),
                password = currentPassword, // ✅ Preserve the actual password from API
                picture = currentUser.photoUrl ?: "", // Fix: use photoUrl instead of picture
                pictureFileSize = null,
                pictureInternalName = null,
                userPreferencesId = userPreferencesId, // 🎯 This is the key field
                isDirty = true,
                isNew = false,
                isMarkedForDeletion = false
            )
            
            // Convert to JSON string like VehicleApi does
            val userJson = gson.toJson(updatedUserDto)
            Log.d(TAG, "Sending User JSON: $userJson")
            
            // Get headers like VehicleApi does
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            // Call API with FormUrlEncoded format like VehicleApi
            val response = userApi.saveUser(
                entity = userJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Failed to update user with UserPreferencesId: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")
                return false
            }
            
            Log.d(TAG, "✅ Successfully updated user with UserPreferencesId")
            val updatedUser = response.body()?.toDomain()
            Log.d(TAG, "Updated user UserPreferencesId: ${updatedUser?.userPreferencesId}")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception updating user with UserPreferencesId: ${e.message}", e)
            false
        }
    }

    override suspend fun updateDefaultBusiness(businessId: String?): UserPreferences {
        val currentPrefs = getCurrentUserPreferences() 
            ?: throw Exception("No current user preferences found")
        
        val updatedPrefs = currentPrefs.copy(businessId = businessId)
        return saveUserPreferences(updatedPrefs)
    }

    override suspend fun updateDefaultSite(siteId: String?): UserPreferences {
        val currentPrefs = getCurrentUserPreferences() 
            ?: throw Exception("No current user preferences found")
        
        val updatedPrefs = currentPrefs.copy(siteId = siteId)
        return saveUserPreferences(updatedPrefs)
    }

    override suspend fun updateLastSelectedBusiness(businessId: String?): UserPreferences {
        val currentPrefs = getCurrentUserPreferences() 
            ?: throw Exception("No current user preferences found")
        
        val updatedPrefs = currentPrefs.copy(lastSelectedBusinessId = businessId)
        return saveUserPreferences(updatedPrefs)
    }

    override suspend fun updateLastSelectedSite(siteId: String?): UserPreferences {
        val currentPrefs = getCurrentUserPreferences() 
            ?: throw Exception("No current user preferences found")
        
        val updatedPrefs = currentPrefs.copy(lastSelectedSiteId = siteId)
        return saveUserPreferences(updatedPrefs)
    }

    override fun observeCurrentUserPreferences(): Flow<UserPreferences?> {
        return _currentPreferences.asStateFlow()
    }

    override suspend fun clearPreferences() {
        _currentPreferences.value = null
        Log.d(TAG, "Preferences cleared")
    }

    override suspend fun getEffectiveBusinessId(): String? {
        return getCurrentUserPreferences()?.getEffectiveBusinessId()
    }

    override suspend fun getEffectiveSiteId(): String? {
        return getCurrentUserPreferences()?.getEffectiveSiteId()
    }

    override suspend fun userNeedsPreferencesSetup(): Boolean {
        return try {
            val preferences = getCurrentUserPreferences()
            if (preferences == null) {
                Log.d(TAG, "User needs preferences setup: No preferences found")
                return true
            }
            
            // Check if user has effective business context
            val hasBusinessContext = preferences.hasBusinessContext()
            Log.d(TAG, "User preferences setup check:")
            Log.d(TAG, "  Has business context: $hasBusinessContext")
            Log.d(TAG, "  Business ID: ${preferences.businessId}")
            Log.d(TAG, "  Last Selected Business: ${preferences.lastSelectedBusinessId}")
            
            !hasBusinessContext
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user needs preferences setup", e)
            true // Default to requiring setup if there's an error
        }
    }

    override suspend fun updateDefaultBusinessId(businessId: String) {
        try {
            Log.d(TAG, "Updating default business ID to: $businessId")
            var currentPrefs = getCurrentUserPreferences()
            
            if (currentPrefs == null) {
                Log.d(TAG, "No current preferences found, creating new preferences")
                // Get current user to create preferences
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    Log.e(TAG, "Cannot create preferences: No current user found")
                    throw Exception("No current user found")
                }
                
                // Create new preferences with the business ID
                currentPrefs = UserPreferences(
                    id = null,
                    user = currentUser,
                    businessId = businessId,
                    siteId = null,
                    lastSelectedBusinessId = businessId,
                    lastSelectedSiteId = null,
                    theme = "system",
                    language = "en",
                    notificationsEnabled = true,
                    isActive = true,
                    isDirty = true,
                    isNew = true
                )
            } else {
                // Update existing preferences
                currentPrefs = currentPrefs.copy(businessId = businessId)
            }
            
            saveUserPreferences(currentPrefs)
            Log.d(TAG, "Successfully updated default business ID to: $businessId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating default business ID", e)
            throw e
        }
    }

    override suspend fun updateDefaultSiteId(siteId: String) {
        try {
            Log.d(TAG, "Updating default site ID to: $siteId")
            var currentPrefs = getCurrentUserPreferences()
            
            if (currentPrefs == null) {
                Log.d(TAG, "No current preferences found, creating new preferences")
                // Get current user to create preferences
                val currentUser = userRepository.getCurrentUser()
                if (currentUser == null) {
                    Log.e(TAG, "Cannot create preferences: No current user found")
                    throw Exception("No current user found")
                }
                
                // Create new preferences with the site ID
                currentPrefs = UserPreferences(
                    id = null,
                    user = currentUser,
                    businessId = null,
                    siteId = siteId,
                    lastSelectedBusinessId = null,
                    lastSelectedSiteId = siteId,
                    theme = "system",
                    language = "en",
                    notificationsEnabled = true,
                    isActive = true,
                    isDirty = true,
                    isNew = true
                )
            } else {
                // Update existing preferences
                currentPrefs = currentPrefs.copy(siteId = siteId)
            }
            
            saveUserPreferences(currentPrefs)
            Log.d(TAG, "Successfully updated default site ID to: $siteId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating default site ID", e)
            throw e
        }
    }

    override suspend fun updateLastSelectedBusinessId(businessId: String) {
        try {
            Log.d(TAG, "Updating last selected business ID to: $businessId")
            var currentPrefs = getCurrentUserPreferences()
            
            if (currentPrefs == null) {
                Log.d(TAG, "No current preferences found, but NOT creating preferences yet")
                Log.d(TAG, "❌ Cannot create preferences with only Business due to DB FK constraints")
                Log.d(TAG, "User must select BOTH Business AND Site before preferences are created")
                
                // ❌ DON'T CREATE PREFERENCES YET - wait for both Business AND Site
                // The database requires BOTH BusinessId AND SiteId due to FK constraints
                // We'll create preferences only when user selects a site (which requires business first)
                
                throw Exception("Cannot create preferences with only Business. Please select a Site as well.")
            } else {
                Log.d(TAG, "Updating existing preferences with new business")
                // Update existing preferences - set both BusinessId and LastSelected
                currentPrefs = currentPrefs.copy(
                    businessId = businessId,
                    lastSelectedBusinessId = businessId,
                    isDirty = true
                )
                
                saveUserPreferences(currentPrefs)
                Log.d(TAG, "✅ Successfully updated last selected business ID to: $businessId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last selected business ID", e)
            throw e
        }
    }

    override suspend fun updateLastSelectedSiteId(siteId: String) {
        try {
            Log.d(TAG, "Updating last selected site ID to: $siteId")
            var currentPrefs = getCurrentUserPreferences()
            
            if (currentPrefs == null) {
                Log.d(TAG, "No current preferences found, but user is selecting site")
                Log.d(TAG, "❌ Cannot create preferences without BusinessId. User must select Business first.")
                throw Exception("Cannot set site without selecting business first")
            } else {
                // ✅ FIXED: Update existing preferences - preserve BusinessId, only update SiteId
                Log.d(TAG, "Preserving existing BusinessId: ${currentPrefs.businessId}")
                currentPrefs = currentPrefs.copy(
                    // businessId = currentPrefs.businessId, // ✅ Keep existing business
                    siteId = siteId, // ✅ Update site
                    lastSelectedSiteId = siteId,
                    isDirty = true
                )
                
                saveUserPreferences(currentPrefs)
                Log.d(TAG, "✅ Successfully updated last selected site ID to: $siteId (preserved BusinessId: ${currentPrefs.businessId})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last selected site ID", e)
            throw e
        }
    }

    /**
     * Create preferences with both BusinessId and SiteId (like working Postman example)
     * If preferences already exist, update them instead of creating new ones
     */
    override suspend fun createPreferencesWithBusinessAndSite(businessId: String, siteId: String): UserPreferences {
        try {
            Log.d(TAG, "Creating/updating preferences with BusinessId: $businessId and SiteId: $siteId")
            
            val currentUser = userRepository.getCurrentUser()
            if (currentUser == null) {
                Log.e(TAG, "Cannot create preferences: No current user found")
                throw Exception("No current user found")
            }
            
            // ✅ Check if preferences already exist for this user
            val existingPrefs = getCurrentUserPreferences()
            
            val prefsToSave = if (existingPrefs != null) {
                Log.d(TAG, "Updating existing preferences instead of creating new ones")
                // Update existing preferences
                existingPrefs.copy(
                    businessId = businessId,
                    siteId = siteId,
                    lastSelectedBusinessId = businessId,
                    lastSelectedSiteId = siteId,
                    isDirty = true,
                    isNew = false // Mark as update, not new
                )
            } else {
                Log.d(TAG, "Creating new preferences")
                // Create new preferences matching working Postman structure
                UserPreferences(
                    id = null,
                    user = currentUser,
                    businessId = businessId, // ✅ Primary business
                    siteId = siteId, // ✅ Primary site
                    lastSelectedBusinessId = businessId, // ✅ Set same as BusinessId for consistency
                    lastSelectedSiteId = siteId, // ✅ Set same as SiteId for consistency
                    theme = "system",
                    language = "en",
                    notificationsEnabled = true,
                    isActive = true,
                    isDirty = true,
                    isNew = true
                )
            }
            
            return saveUserPreferences(prefsToSave)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating/updating preferences with business and site", e)
            throw e
        }
    }

    private suspend fun createDefaultPreferences(userId: String): UserPreferences {
        Log.d(TAG, "Creating default preferences for user: $userId")
        
        // Get the user object to include in preferences
        val user = userRepository.getUserById(userId)
        
        val defaultPrefs = UserPreferences(
            id = null, // Will be generated by backend
            user = user, // Include complete user object
            businessId = null,
            siteId = null,
            lastSelectedBusinessId = null,
            lastSelectedSiteId = null,
            theme = "system",
            language = "en",
            notificationsEnabled = true,
            isActive = true,
            isDirty = true,
            isNew = true
        )
        
        // Save the default preferences and return the saved version
        return try {
            Log.d(TAG, "Saving default preferences to backend")
            saveUserPreferences(defaultPrefs)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save default preferences to backend, returning in-memory version", e)
            defaultPrefs
        }
    }
} 