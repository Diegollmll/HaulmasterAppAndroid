package app.forku.data.repository.user

import android.util.Log
import app.forku.data.api.UserApi
import app.forku.data.api.GOUserRoleApi
import app.forku.data.api.dto.user.UserDto
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.getUserPreferencesId
import app.forku.data.mapper.hasUserPreferences
import app.forku.data.mapper.toDomain as userPreferencesToDomain
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDto
import app.forku.domain.model.user.UserRole
import app.forku.data.local.TourPreferences
import app.forku.core.auth.UserRoleManager
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import app.forku.data.repository.BaseRepository
import app.forku.core.auth.TokenErrorHandler
import app.forku.data.api.GOSecurityProviderApi
import app.forku.core.auth.HeaderManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import app.forku.data.api.GOServicesApi
import app.forku.data.api.UserPreferencesApi

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val goUserRoleApi: GOUserRoleApi,
    authDataStore: AuthDataStore,
    private val tourPreferences: TourPreferences,
    tokenErrorHandler: TokenErrorHandler,
    private val securityApi: GOSecurityProviderApi,
    private val headerManager: HeaderManager,
    private val goServicesApi: GOServicesApi,
    private val userPreferencesApi: UserPreferencesApi
) : UserRepository, BaseRepository(authDataStore, tokenErrorHandler) {
    
    private val TAG = "appflow UserRepository"
    
    // Temporary cache for user-site mappings from last API call
    private var cachedUserSiteMappings: Map<String, List<String>> = emptyMap()
    
    private suspend fun getUserRoleFromGOApi(userId: String): UserRole {
        try {
            val response = goUserRoleApi.getUserRoles()
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user roles: ${response.code()}")
                return UserRole.OPERATOR
            }

            val userRoles = response.body() ?: return UserRole.OPERATOR
            val userRole = userRoles.find { it.GOUserId == userId }?.gORoleName
                ?: return UserRole.OPERATOR

            return UserRoleManager.fromString(userRole)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user role: ${e.message}")
            return UserRole.OPERATOR
        }
    }

    override suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Getting user by ID: $userId")
            // Include userRoleItems, userBusinesses, userSiteItems, and userPreferencesId to get all user data
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = userId,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            Log.d("UserRepository", "API response code: ${response.code()}")
            
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user: ${response.code()}")
                return@withContext null
            }

            val userDto = response.body()
            if (userDto == null) {
                Log.e("UserRepository", "User DTO is null for ID: $userId")
                return@withContext null
            }

            Log.d("UserRepository", "Received user DTO: id=${userDto.id}, userRoleItems count: ${userDto.userRoleItems?.size ?: 0}, userBusinesses count: ${userDto.userBusinesses?.size ?: 0}, userSiteItems count: ${userDto.userSiteItems?.size ?: 0}")
            
            // Map to domain model using the included data
            val user = userDto.toDomain()
            Log.d("UserRepository", "Mapped user: id=${user.id}, role=${user.role}, businessId=${user.businessId}, siteId=${user.siteId}, photoUrl=${user.photoUrl}")
            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user: ${e.message}", e)
            null
        }
    }

//    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
//        // Delegar la autenticación exclusivamente al repositorio de seguridad
//        goSecurityProviderRepository.authenticate(email, password)
//    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Verificar si el usuario ya existe buscando en la lista de usuarios
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val existingUsers = api.getUsers(csrfToken = csrfToken, cookie = cookie)
            if (!existingUsers.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to check existing users"))
            }

            val userExists = existingUsers.body()?.any { it.email == email } ?: false
            if (userExists) {
                return@withContext Result.failure(Exception("User already exists"))
            }

            // Create new user with the provided role
            val newUser = UserDto(
                id = UUID.randomUUID().toString(),
                email = email,
                password = password,
                username = email,
                firstName = firstName,
                lastName = lastName,
                picture = "",
                pictureFileSize = null,
                pictureInternalName = null
            )

            // Convert to JSON string for entity parameter
            val gson = com.google.gson.Gson()
            val userJson = gson.toJson(newUser)
            
            val response = api.createUser(
                entity = userJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Registration failed"))
            }

            val createdUser = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Failed to create user"))

            // Guardar usuario en AuthDataStore
            authDataStore.setCurrentUser(createdUser)
            
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return authDataStore.getCurrentUser()
    }

    /**
     * Get current user with preferences included from API
     */
    suspend fun getCurrentUserWithPreferences(): Pair<User?, app.forku.domain.model.user.UserPreferences?> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUser()
            if (currentUser == null) {
                Log.w(TAG, "No current user found in local storage")
                return@withContext Pair(null, null)
            }
            
            Log.d(TAG, "Getting current user from API to check UserPreferencesId: ${currentUser.id}")
            
            // Get user from API to get UserPreferencesId
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = currentUser.id,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to get user from API: ${response.code()}")
                return@withContext Pair(currentUser, null) // Return cached user, no preferences
            }
            
            val userDto = response.body()
            if (userDto == null) {
                Log.e(TAG, "User DTO is null")
                return@withContext Pair(currentUser, null)
            }
            
            // Check if user has UserPreferencesId
            val userPreferencesId = userDto.getUserPreferencesId()
            Log.d(TAG, "User preferences status:")
            Log.d(TAG, "  Has UserPreferencesId: ${userDto.hasUserPreferences()}")
            Log.d(TAG, "  UserPreferencesId: $userPreferencesId")
            
            // Treat empty GUID as null/blank
            val isEmptyGuid = userPreferencesId == "00000000-0000-0000-0000-000000000000"
            if (userPreferencesId.isNullOrBlank() || isEmptyGuid) {
                Log.d(TAG, "User has no valid UserPreferencesId (null, blank, or empty GUID), needs setup")
                return@withContext Pair(userDto.toDomain(), null)
            }
            
            // Fetch UserPreferences by ID
            Log.d(TAG, "Fetching UserPreferences by ID: $userPreferencesId")
            try {
                val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
                val preferencesResponse = userPreferencesApi.getUserPreferencesById(
                    id = userPreferencesId,
                    csrfToken = csrfToken,
                    cookie = cookie
                )
                
                if (!preferencesResponse.isSuccessful) {
                    Log.w(TAG, "Failed to fetch UserPreferences by ID: ${preferencesResponse.code()}")
                    return@withContext Pair(userDto.toDomain(), null)
                }
                
                val preferencesDto = preferencesResponse.body()
                if (preferencesDto == null) {
                    Log.w(TAG, "UserPreferences response body is null")
                    return@withContext Pair(userDto.toDomain(), null)
                }
                
                val preferences = preferencesDto.userPreferencesToDomain()
                Log.d(TAG, "Successfully fetched UserPreferences:")
                Log.d(TAG, "  Business ID: ${preferences.businessId}")
                Log.d(TAG, "  Site ID: ${preferences.siteId}")
                Log.d(TAG, "  Last Selected Business: ${preferences.lastSelectedBusinessId}")
                Log.d(TAG, "  Last Selected Site: ${preferences.lastSelectedSiteId}")
                Log.d(TAG, "  Effective context: businessId=${preferences.getEffectiveBusinessId()}, siteId=${preferences.getEffectiveSiteId()}")
                
                return@withContext Pair(userDto.toDomain(), preferences)
                
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch UserPreferences by ID: $userPreferencesId", e)
                return@withContext Pair(userDto.toDomain(), null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user with preferences", e)
            Pair(getCurrentUser(), null) // Return cached user as fallback
        }
    }

    override suspend fun refreshCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUser() ?: return@withContext Result.failure(Exception("No user logged in"))
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = currentUser.id,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            
            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    val updatedUser = userDto.toDomain()
                    authDataStore.setCurrentUser(updatedUser)
                    Result.success(updatedUser)
                } ?: Result.failure(Exception("User refresh response was empty"))
            } else {
                Result.failure(Exception("Failed to refresh user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        updatePresence(false)
        authDataStore.clearAuth()
    }

    override suspend fun updateUserRole(userId: String, newRole: UserRole): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            
            val updatedUserDto = UserDto(
                id = user.id,
                email = user.email.takeIf { it.isNotBlank() && it != "null" }, // ✅ FIXED: Don't send empty/null email
                password = "", // No incluimos el password en la actualización
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                picture = "",
                pictureFileSize = null,
                pictureInternalName = null
            )

            // ✅ NEW: Detailed logging of updatedUserDto in updateUserRole
            Log.d("UserRepository", "=== UPDATE USER ROLE DTO DEBUG ===")
            Log.d("UserRepository", "  id: '${updatedUserDto.id}'")
            Log.d("UserRepository", "  email: '${updatedUserDto.email}'")
            Log.d("UserRepository", "  username: '${updatedUserDto.username}'")
            Log.d("UserRepository", "  firstName: '${updatedUserDto.firstName}'")
            Log.d("UserRepository", "  lastName: '${updatedUserDto.lastName}'")
            Log.d("UserRepository", "  password: '${updatedUserDto.password?.take(10)}...' (length: ${updatedUserDto.password?.length ?: 0})")
            Log.d("UserRepository", "  picture: '${updatedUserDto.picture}'")
            Log.d("UserRepository", "  userPreferencesId: '${updatedUserDto.userPreferencesId}'")
            Log.d("UserRepository", "  isDirty: ${updatedUserDto.isDirty}")
            Log.d("UserRepository", "  isNew: ${updatedUserDto.isNew}")
            Log.d("UserRepository", "  isMarkedForDeletion: ${updatedUserDto.isMarkedForDeletion}")
            Log.d("UserRepository", "=====================================")

            // Convert to JSON string and get headers like the working pattern
            val gson = com.google.gson.Gson()
            val userJson = gson.toJson(updatedUserDto)
            
            // ✅ NEW: Log the complete JSON being sent in updateUserRole
            Log.d("UserRepository", "=== UPDATE USER ROLE JSON BEING SENT ===")
            Log.d("UserRepository", "userJson: $userJson")
            Log.d("UserRepository", "JSON length: ${userJson.length}")
            Log.d("UserRepository", "=========================================")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            
            val response = api.saveUser(
                entity = userJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to update user role"))
            }

            val updatedUser = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Failed to get updated user"))

            // Si el usuario actualizado es el usuario actual, actualizar en AuthDataStore
            getCurrentUser()?.let { currentUser ->
                if (currentUser.id == userId) {
                    authDataStore.setCurrentUser(updatedUser)
                }
            }

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Unit = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Updating user: ${user.id}")
            Log.d("UserRepository", "Update details: isApproved=${user.isApproved}, role=${user.role}")
            
            // Get the current user data to preserve the password
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val currentUserResponse = api.getUser(
                id = user.id,
                csrfToken = csrfToken,
                cookie = cookie
            )
            val currentUserData = currentUserResponse.body()
            val currentPassword = currentUserData?.password
            
            Log.d("UserRepository", "Password preservation check:")
            Log.d("UserRepository", "  API response successful: ${currentUserResponse.isSuccessful}")
            Log.d("UserRepository", "  Current user data found: ${currentUserData != null}")
            Log.d("UserRepository", "  Password found: ${!currentPassword.isNullOrBlank()}")
            Log.d("UserRepository", "  Password length: ${currentPassword?.length ?: 0}")
            
            // ✅ CRITICAL FIX: Use email from API if available, otherwise from parameter if valid
            val emailFromApi = currentUserData?.email?.takeIf { it.isNotBlank() && it != "null" }
            val emailFromParameter = user.email.takeIf { email ->
                email.isNotBlank() && 
                email != "null" && 
                email.contains("@") && 
                email.contains(".")
            }
            
            // Priority: API email > Parameter email > null
            val validEmail = emailFromApi ?: emailFromParameter
            
            Log.d("UserRepository", "Email resolution in updateUser:")
            Log.d("UserRepository", "  Email from API: '$emailFromApi'")
            Log.d("UserRepository", "  Email from parameter: '$emailFromParameter'")
            Log.d("UserRepository", "  Final email: '$validEmail'")
            
            // ⚠️ CRITICAL FIX: Don't include password field if it's null/empty to avoid overwriting
            // Only include password if we have a valid one from the API
            val userDto = if (!currentPassword.isNullOrBlank()) {
                UserDto(
                    id = user.id,
                    email = validEmail, // ✅ Only send if it's a valid email
                    password = currentPassword, // Only include if we have a valid password
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    picture = "",
                    pictureFileSize = null,
                    pictureInternalName = null
                )
            } else {
                // Don't include password field at all if we don't have one
                UserDto(
                    id = user.id,
                    email = validEmail, // ✅ Only send if it's a valid email
                    password = null, // Explicitly set to null to avoid sending empty string
                    username = user.username,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    picture = "",
                    pictureFileSize = null,
                    pictureInternalName = null
                )
            }

            // ✅ NEW: Detailed logging of userDto in updateUser
            Log.d("UserRepository", "=== UPDATE USER DTO DEBUG ===")
            Log.d("UserRepository", "  id: '${userDto.id}'")
            Log.d("UserRepository", "  email: '${userDto.email}'")
            Log.d("UserRepository", "  username: '${userDto.username}'")
            Log.d("UserRepository", "  firstName: '${userDto.firstName}'")
            Log.d("UserRepository", "  lastName: '${userDto.lastName}'")
            Log.d("UserRepository", "  password: '${userDto.password?.take(10)}...' (length: ${userDto.password?.length ?: 0})")
            Log.d("UserRepository", "  picture: '${userDto.picture}'")
            Log.d("UserRepository", "  userPreferencesId: '${userDto.userPreferencesId}'")
            Log.d("UserRepository", "  isDirty: ${userDto.isDirty}")
            Log.d("UserRepository", "  isNew: ${userDto.isNew}")
            Log.d("UserRepository", "  isMarkedForDeletion: ${userDto.isMarkedForDeletion}")
            Log.d("UserRepository", "=================================")

            Log.d("UserRepository", "Sending update request to API")
            
            // Convert to JSON string and get headers like the working pattern
            val gson = com.google.gson.Gson()
            val userJson = gson.toJson(userDto)
            
            // ✅ NEW: Log the complete JSON being sent in updateUser
            Log.d("UserRepository", "=== UPDATE USER JSON BEING SENT ===")
            Log.d("UserRepository", "userJson: $userJson")
            Log.d("UserRepository", "JSON length: ${userJson.length}")
            Log.d("UserRepository", "=====================================")
            // ✅ FIXED: Reuse existing csrfToken and cookie instead of redeclaring
            
            val response = api.saveUser(
                entity = userJson,
                csrfToken = csrfToken,
                cookie = cookie
            )
            
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to update user: ${response.code()}")
                Log.e("UserRepository", "Error body: ${response.errorBody()?.string()}")
                throw Exception("Failed to update user: ${response.code()}")
            }

            Log.d("UserRepository", "User updated successfully")
            
            // Si el usuario actualizado es el usuario actual, actualizar en AuthDataStore
            getCurrentUser()?.let { currentUser ->
                if (currentUser.id == user.id) {
                    Log.d("UserRepository", "Updating current user in AuthDataStore")
                    authDataStore.setCurrentUser(user)
                }
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user", e)
            throw e
        }
    }

    override suspend fun getUsersByRole(role: UserRole): List<User> = withContext(Dispatchers.IO) {
        try {
            // Use include parameter to fetch user roles, businesses, and sites in one call
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUsers(
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get users with roles: ${response.code()}")
                return@withContext emptyList()
            }

            val users = response.body()
                ?.map { userDto ->
                    Log.d("UserRepository", "Processing user with included roles: ${userDto.id}, roles: ${userDto.userRoleItems?.size ?: 0}, businesses: ${userDto.userBusinesses?.size ?: 0}, sites: ${userDto.userSiteItems?.size ?: 0}")
                    userDto.toDomain()
                }
                ?.filter { user -> 
                    val matches = user.role == role
                    Log.d("UserRepository", "User ${user.id} role ${user.role} matches filter $role: $matches")
                    matches
                }
                ?: emptyList()
            
            Log.d("UserRepository", "Returning ${users.size} users with role $role")
            users
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting users by role: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getAllUsers(include: String?): List<User> = withContext(Dispatchers.IO) {
        executeApiCallForList {
            Log.d("UserRepository", "Starting getAllUsers request with include: $include")
            
            // Log the API endpoint being called
            Log.d("UserRepository", "Making API call to: api/gouser/list")
            
            // Log any stored tokens for debugging auth issues
            val applicationToken = authDataStore.getApplicationToken()
            val authToken = authDataStore.getAuthenticationToken()
            val storedCsrfToken = authDataStore.getCsrfToken()
            Log.d("UserRepository", """
                Auth state:
                - Application token present: ${applicationToken != null}
                - Auth token present: ${authToken != null}
                - CSRF token present: ${storedCsrfToken != null}
            """.trimIndent())
            
            val includeParam = include ?: "UserRoleItems,UserBusinesses,UserSiteItems"
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUsers(
                csrfToken = csrfToken,
                cookie = cookie,
                include = includeParam
            )
            
            // Add detailed logging of API response
            Log.d("UserRepository", "API Response:")
            Log.d("UserRepository", "- Status: ${response.code()}")
            Log.d("UserRepository", "- Success: ${response.isSuccessful}")
            Log.d("UserRepository", "- Body size: ${response.body()?.size ?: 0}")
            
            // Cache user-site mappings for filtering
            val userSiteMappings = mutableMapOf<String, List<String>>()
            
            response.body()?.forEachIndexed { index, userDto ->
                Log.d("UserRepository", "User $index: id=${userDto.id}, roles=${userDto.userRoleItems?.size ?: 0}, sites=${userDto.userSiteItems?.size ?: 0}, businesses=${userDto.userBusinesses?.size ?: 0}")
                if (!userDto.userRoleItems.isNullOrEmpty()) {
                    userDto.userRoleItems!!.forEach { roleItem ->
                        Log.d("UserRepository", "  - Role: ${roleItem.gORoleName}, Active: ${roleItem.isActive}")
                    }
                }
                userDto.userSiteItems?.forEach { siteItem ->
                    Log.d("UserRepository", "  - SiteItem: siteId=${siteItem.siteId}, goUserId=${siteItem.goUserId}")
                }
                userDto.userBusinesses?.forEach { businessItem ->
                    Log.d("UserRepository", "  - BusinessItem: businessId=${businessItem.businessId}, siteId=${businessItem.siteId}")
                }
                
                // Cache user-site mappings
                userDto.id?.let { userId ->
                    val siteIds = userDto.userSiteItems?.map { it.siteId } ?: emptyList()
                    userSiteMappings[userId] = siteIds
                }
            }
            
            // Update cached mappings
            cachedUserSiteMappings = userSiteMappings
            Log.d("UserRepository", "Cached user-site mappings: $cachedUserSiteMappings")
            
            response
        }.map { userDto ->
            Log.d("UserRepository", "Processing user DTO: id=${userDto.id}, email=${userDto.email}")
            userDto.toDomain().also { user ->
                Log.d("UserRepository", "Mapped to domain user: id=${user.id}, role=${user.role}")
            }
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.deleteUser(
                id = userId,
                csrfToken = csrfToken,
                cookie = cookie
            )
            if (!response.isSuccessful) {
                throw Exception("Failed to delete user")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun searchUsers(query: String): List<User> {
        return try {
            val allUsers = getAllUsers()
            allUsers.filter { user ->
                user.fullName.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun observeCurrentUser(): Flow<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun getTourCompletionStatus(): Boolean {
        return tourPreferences.hasTourCompleted()
    }

    override suspend fun setTourCompleted() {
        tourPreferences.setTourCompleted()
    }

    override suspend fun getAuthToken(): String? {
        return authDataStore.getCurrentUser()?.token
    }

    override suspend fun updatePresence(isOnline: Boolean) {
        authDataStore.updatePresence(isOnline)
    }

    override suspend fun getLastActiveTime(userId: String): Long? {
        val user = getUserById(userId)
        return user?.lastLogin?.toLongOrNull()
    }

    override suspend fun getCurrentUserId(): String? {
        // First try to get from current user
        val currentUser = authDataStore.getCurrentUser()
        if (currentUser != null) {
            return currentUser.id
        }
        
        // If current user is null, try to extract from application token
        try {
            Log.d("UserRepository", "Current user is null, attempting to extract user ID from token")
            val applicationToken = authDataStore.getApplicationToken()
            if (!applicationToken.isNullOrEmpty()) {
                val tokenClaims = app.forku.data.api.auth.TokenParser.parseJwtToken(applicationToken)
                Log.d("UserRepository", "Extracted user ID from token: ${tokenClaims.userId}")
                return tokenClaims.userId
            } else {
                Log.w("UserRepository", "No application token available")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error extracting user ID from token", e)
        }
        
        return null
    }

    override suspend fun getUnassignedUsers(): List<User> {
        Log.d("UserRepository", "Getting unassigned users")
        return try {
            // Obtener todos los usuarios
            val allUsers = getAllUsers()
            Log.d("UserRepository", "Total users: ${allUsers.size}")
            
            // Filter users that don't have businessId or businessId is empty
            val unassignedUsers = allUsers.filter { user ->
                user.businessId.isNullOrEmpty()
            }
            Log.d("UserRepository", "Unassigned users: ${unassignedUsers.size}")
            
            unassignedUsers
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting unassigned users", e)
            emptyList()
        }
    }

    override suspend fun getUserCount(): Int? = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Getting user count from API")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUserCount(csrfToken = csrfToken, cookie = cookie)
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user count: ${response.code()}")
                Log.d("UserRepository", "Falling back to counting all users")
                val users = getAllUsers()
                Log.d("UserRepository", "Fallback count: ${users.size} users")
                return@withContext users.size
            }
            
            val count = response.body()
            Log.d("UserRepository", "User count from API: $count")
            return@withContext count
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user count, falling back to getAllUsers()", e)
            // Fallback to getting all users and counting them
            try {
                val users = getAllUsers()
                Log.d("UserRepository", "Fallback count after exception: ${users.size} users")
                return@withContext users.size
            } catch (e2: Exception) {
                Log.e("UserRepository", "Error in fallback count", e2)
                return@withContext null
            }
        }
    }

    override suspend fun getUserWithBusinesses(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Getting user with businesses and sites: $userId")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = userId,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user with businesses: ${response.code()}")
                return@withContext null
            }

            val userDto = response.body()
            if (userDto == null) {
                Log.e("UserRepository", "User DTO is null for ID: $userId")
                return@withContext null
            }

            Log.d("UserRepository", "User businesses: ${userDto.userBusinesses?.size ?: 0}")
            Log.d("UserRepository", "User sites: ${userDto.userSiteItems?.size ?: 0}")
            userDto.userBusinesses?.forEach { business ->
                Log.d("UserRepository", "Business: ${business.businessId}, Site: ${business.siteId}")
            }
            
            // Extract first business ID and save to DataStore
            val businessId = userDto.userBusinesses?.firstOrNull()?.businessId
            if (businessId != null) {
                Log.d("UserRepository", "Saving business context: $businessId")
                authDataStore.saveCurrentBusinessId(businessId)
            } else {
                Log.d("UserRepository", "No business found for user")
                authDataStore.clearBusinessContext()
            }
            
            userDto.toDomain()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user with businesses", e)
            null
        }
    }
    
    override suspend fun getCurrentUserBusinessId(): String? = withContext(Dispatchers.IO) {
        try {
            authDataStore.getCurrentBusinessId()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current business ID: ${e.message}", e)
            null
        }
    }
    
    override suspend fun updateCurrentUser(user: User): Unit = withContext(Dispatchers.IO) {
        try {
            authDataStore.setCurrentUser(user)
            Log.d("UserRepository", "Current user updated in AuthDataStore: ${user.username}")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating current user", e)
            throw e
        }
    }

    override suspend fun getCurrentUserAssignedSites(): List<String> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId == null) {
                Log.w("UserRepository", "No current user ID found")
                return@withContext emptyList()
            }
            
            Log.d("UserRepository", "Getting assigned sites for user: $currentUserId")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = currentUserId,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user with sites: ${response.code()}")
                return@withContext emptyList()
            }

            val userDto = response.body()
            if (userDto == null) {
                Log.e("UserRepository", "User DTO is null for current user")
                return@withContext emptyList()
            }

            Log.d("UserRepository", "Raw UserSiteItems from API: ${userDto.userSiteItems}")
            Log.d("UserRepository", "UserSiteItems size: ${userDto.userSiteItems?.size ?: 0}")
            
            userDto.userSiteItems?.forEach { userSite ->
                Log.d("UserRepository", "UserSite: id=${userSite.id}, siteId=${userSite.siteId}, goUserId=${userSite.goUserId}")
            }

            // Extract site IDs from UserSiteItems
            val siteIds = userDto.userSiteItems?.map { it.siteId }?.distinct() ?: emptyList()
            
            Log.d("UserRepository", "Extracted site IDs: $siteIds")
            Log.d("UserRepository", "Found ${siteIds.size} assigned sites for user: $siteIds")
            siteIds
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user assigned sites", e)
            emptyList()
        }
    }

    override suspend fun getCurrentUserAssignedBusinesses(): List<String> = withContext(Dispatchers.IO) {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId == null) {
                Log.w("UserRepository", "No current user ID found")
                return@withContext emptyList()
            }
            
            Log.d("UserRepository", "Getting assigned businesses for user: $currentUserId")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = currentUserId,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user with businesses: ${response.code()}")
                return@withContext emptyList()
            }

            val userDto = response.body()
            if (userDto == null) {
                Log.e("UserRepository", "User DTO is null for current user")
                return@withContext emptyList()
            }

            Log.d("UserRepository", "Raw UserBusinesses from API: ${userDto.userBusinesses}")
            Log.d("UserRepository", "UserBusinesses size: ${userDto.userBusinesses?.size ?: 0}")
            
            userDto.userBusinesses?.forEach { userBusiness ->
                Log.d("UserRepository", "UserBusiness: businessId=${userBusiness.businessId}, siteId=${userBusiness.siteId}")
            }

            // Extract business IDs from UserBusinesses
            val businessIds = userDto.userBusinesses?.map { it.businessId }?.distinct() ?: emptyList()
            
            Log.d("UserRepository", "Extracted business IDs: $businessIds")
            Log.d("UserRepository", "Found ${businessIds.size} assigned businesses for user: $businessIds")
            businessIds
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting assigned businesses", e)
            emptyList()
        }
    }

    override suspend fun getUserSiteMappings(): Map<String, List<String>> {
        Log.d("UserRepository", "Returning cached user-site mappings: $cachedUserSiteMappings")
        return cachedUserSiteMappings
    }

    suspend fun getUserFromGOApi(userId: String): Result<User> {
        return try {
            Log.d("UserRepository", "Fetching user from GO API for userId: $userId")
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = api.getUser(
                id = userId,
                csrfToken = csrfToken,
                cookie = cookie,
                include = "UserRoleItems,UserBusinesses,UserSiteItems"
            )
            Log.d("UserRepository", "GO API Response: $response")
            
            if (response.isSuccessful) {
                val userDto = response.body()
                Log.d("UserRepository", "User DTO received: $userDto")
                
                if (userDto != null) {
                    val user = userDto.toDomain()
                    Log.d("UserRepository", "Mapped user: id=${user.id}, firstName=${user.firstName}, lastName=${user.lastName}, fullName=${user.fullName}, photoUrl=${user.photoUrl}, role=${user.role}")
                    Result.success(user)
                } else {
                    Log.e("UserRepository", "User DTO is null")
                    Result.failure(Exception("User not found"))
                }
            } else {
                Log.e("UserRepository", "API call failed with code: ${response.code()}")
                Result.failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user", e)
            Result.failure(e)
        }
    }
} 