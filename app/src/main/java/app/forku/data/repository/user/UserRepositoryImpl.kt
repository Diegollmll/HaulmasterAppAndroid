package app.forku.data.repository.user

import android.util.Log
import app.forku.data.api.UserApi
import app.forku.data.api.GOUserRoleApi
import app.forku.data.api.dto.user.UserDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDto
import app.forku.domain.model.user.UserRole
import app.forku.data.local.TourPreferences
import app.forku.core.auth.RoleConverter
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

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val goUserRoleApi: GOUserRoleApi,
    authDataStore: AuthDataStore,
    private val tourPreferences: TourPreferences,
    tokenErrorHandler: TokenErrorHandler,
    private val securityApi: GOSecurityProviderApi,
    private val headerManager: HeaderManager,
    private val goServicesApi: GOServicesApi
) : UserRepository, BaseRepository(authDataStore, tokenErrorHandler) {
    
    private val TAG = "appflow UserRepository"
    
    private suspend fun getUserRoleFromGOApi(userId: String): UserRole {
        try {
            val response = goUserRoleApi.getUserRoles()
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user roles: ${response.code()}")
                return UserRole.OPERATOR
            }

            val userRoles = response.body() ?: return UserRole.OPERATOR
            val userRole = userRoles.find { it.GOUserId == userId }?.role?.Name
                ?: return UserRole.OPERATOR

            return RoleConverter.fromString(userRole)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user role: ${e.message}")
            return UserRole.OPERATOR
        }
    }

    override suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            Log.d("UserRepository", "Getting user by ID: $userId")
            val response = api.getUser(userId)
            Log.d("UserRepository", "API response code: ${response.code()}")
            Log.d("UserRepository", "API response body: ${response.body()}")
            Log.d("UserRepository", "API response headers: ${response.headers()}")
            Log.d("UserRepository", "API response error body: ${response.errorBody()?.string()}")
            
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user: ${response.code()}")
                return@withContext null
            }

            val userDto = response.body()
            if (userDto == null) {
                Log.e("UserRepository", "User DTO is null for ID: $userId")
                return@withContext null
            }

            Log.d("UserRepository", "Received user DTO: id=${userDto.id}, picture=${userDto.picture}, pictureInternalName=${userDto.pictureInternalName}")
            Log.d("UserRepository", "UserDto for Ana: id=${userDto.id}, picture=${userDto.picture}, pictureInternalName=${userDto.pictureInternalName}")
            val userRole = getUserRoleFromGOApi(userId)
            
            // Map to domain model, passing the role directly
            val user = userDto.toDomain(userRole)
            Log.d("UserRepository", "Mapped user: id=${user.id}, photoUrl=${user.photoUrl}")
            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user: ${e.message}", e)
            null
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch CSRF token and antiforgery cookie from GOServicesApi
            val csrfResponse = goServicesApi.getCsrfToken()
            if (!csrfResponse.isSuccessful) {
                Log.e(TAG, "Failed to fetch CSRF token: ${csrfResponse.code()}")
                return@withContext Result.failure(Exception("No se pudo obtener el token CSRF. Intenta de nuevo."))
            }
            val csrfToken = csrfResponse.body()?.csrfToken
            val cookieHeader = csrfResponse.headers()["Set-Cookie"]
            if (csrfToken.isNullOrBlank() || cookieHeader.isNullOrBlank()) {
                Log.e(TAG, "CSRF token or antiforgery cookie missing in response")
                return@withContext Result.failure(Exception("No se pudo obtener el token CSRF o la cookie antiforgery."))
            }
            authDataStore.saveCsrfToken(csrfToken)
            authDataStore.saveAntiforgeryCookie(cookieHeader)

            // 2. Prepare multipart form data
            val usernamePart = email.toRequestBody("text/plain".toMediaType())
            val passwordPart = password.toRequestBody("text/plain".toMediaType())
            val useCookiesPart = "true".toRequestBody("text/plain".toMediaType())

            // 3. Use the freshly fetched CSRF token and cookie
            val response = securityApi.authenticate(
                csrfToken = csrfToken,
                cookie = cookieHeader,
                username = usernamePart,
                password = passwordPart,
                useCookies = useCookiesPart
            )

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    val applicationToken = authResponse.getApplicationToken()
                    val authenticationToken = authResponse.getAuthenticationToken()

                    if (applicationToken != null && authenticationToken != null) {
                        // Save tokens
                        authDataStore.saveApplicationToken(applicationToken)
                        authDataStore.saveAuthenticationToken(authenticationToken)
                        authDataStore.logTokenExpirationDate()

                        // Parse user from token
                        val tokenClaims = app.forku.data.api.auth.TokenParser.parseJwtToken(applicationToken)
                        val user = app.forku.domain.model.user.User(
                            id = tokenClaims.userId,
                            email = email,
                            username = tokenClaims.username,
                            firstName = tokenClaims.username,
                            lastName = tokenClaims.familyName.ifEmpty { "" },
                            token = applicationToken,
                            refreshToken = authenticationToken,
                            photoUrl = null,
                            role = tokenClaims.role,
                            password = password,
                            certifications = emptyList(),
                            lastMedicalCheck = null,
                            lastLogin = System.currentTimeMillis().toString(),
                            isActive = true,
                            isApproved = true,
                            businessId = null,
                            siteId = null,
                            systemOwnerId = null
                        )
                        authDataStore.setCurrentUser(user)
                        tokenErrorHandler.resetAuthenticationState()
                        Result.success(user)
                    } else {
                        Log.e(TAG, "No tokens found in response")
                        Result.failure(Exception("No tokens found in response"))
                    }
                } else {
                    Log.e(TAG, "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Authentication failed. Status: "+response.code()+", Error: "+errorBody)
                Result.failure(Exception("Authentication failed: "+response.code()+" - "+errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Verificar si el usuario ya existe buscando en la lista de usuarios
            val existingUsers = api.getUsers()
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

            val response = api.createUser(newUser)
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

    override suspend fun refreshCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val currentUser = getCurrentUser() ?: return@withContext Result.failure(Exception("No user logged in"))
            val response = api.getUser(currentUser.id)
            
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
                email = user.email,
                password = "", // No incluimos el password en la actualización
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                picture = "",
                pictureFileSize = null,
                pictureInternalName = null
            )

            val response = api.saveUser(updatedUserDto)
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
            val currentUserData = api.getUser(user.id).body()
            val currentPassword = currentUserData?.password ?: ""
            
            val userDto = UserDto(
                id = user.id,
                email = user.email,
                password = currentPassword, // Preserve the current password
                username = user.username,
                firstName = user.firstName,
                lastName = user.lastName,
                picture = "",
                pictureFileSize = null,
                pictureInternalName = null
            )

            Log.d("UserRepository", "Sending update request to API")
            val response = api.saveUser(userDto)
            
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
            val response = api.getUsers()
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            response.body()
                ?.map { it.toDomain() }
                ?.filter { it.role == role }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        executeApiCallForList {
            Log.d("UserRepository", "Starting getAllUsers request")
            
            // Log the API endpoint being called
            Log.d("UserRepository", "Making API call to: api/gouser/list")
            
            // Log any stored tokens for debugging auth issues
            val applicationToken = authDataStore.getApplicationToken()
            val authToken = authDataStore.getAuthenticationToken()
            val csrfToken = authDataStore.getCsrfToken()
            Log.d("UserRepository", """
                Auth state:
                - Application token present: ${applicationToken != null}
                - Auth token present: ${authToken != null}
                - CSRF token present: ${csrfToken != null}
            """.trimIndent())
            
            api.getUsers()
        }.map { userDto ->
            Log.d("UserRepository", "Processing user DTO: id=${userDto.id}, email=${userDto.email}")
            userDto.toDomain().also { user ->
                Log.d("UserRepository", "Mapped to domain user: id=${user.id}, role=${user.role}")
            }
        }
    }

    override suspend fun deleteUser(userId: String) {
        try {
            val response = api.deleteUser(userId)
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
        return authDataStore.getCurrentUser()?.id
    }

    override suspend fun getUnassignedUsers(): List<User> {
        Log.d("UserRepository", "Getting unassigned users")
        return try {
            // Obtener todos los usuarios
            val allUsers = getAllUsers()
            Log.d("UserRepository", "Total users: ${allUsers.size}")
            
            // Filtrar usuarios que no tienen businessId o businessId está vacío
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
            val response = api.getUserCount()
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

    suspend fun getUserFromGOApi(userId: String): Result<User> {
        return try {
            Log.d("UserRepository", "Fetching user from GO API for userId: $userId")
            val response = api.getUser(userId)
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