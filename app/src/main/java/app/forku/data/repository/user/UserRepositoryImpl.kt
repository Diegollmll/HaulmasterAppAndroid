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
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import app.forku.data.repository.BaseRepository
import app.forku.core.auth.TokenErrorHandler

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: UserApi,
    private val goUserRoleApi: GOUserRoleApi,
    authDataStore: AuthDataStore,
    private val tourPreferences: TourPreferences,
    tokenErrorHandler: TokenErrorHandler
) : UserRepository, BaseRepository(authDataStore, tokenErrorHandler) {
    
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

            return when (userRole.lowercase()) {
                "administrator" -> UserRole.SYSTEM_OWNER
                "admin" -> UserRole.ADMIN
                "operator" -> UserRole.OPERATOR
                "superadmin" -> UserRole.SUPERADMIN
                "systemowner", "system_owner" -> UserRole.SYSTEM_OWNER
                else -> UserRole.OPERATOR
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user role: ${e.message}")
            return UserRole.OPERATOR
        }
    }

    override suspend fun getUserById(userId: String): User? = withContext(Dispatchers.IO) {
        try {
            val response = api.getUser(userId)
            if (!response.isSuccessful) {
                Log.e("UserRepository", "Failed to get user: ${response.code()}")
                return@withContext null
            }

            val userDto = response.body() ?: return@withContext null
            val userRole = getUserRoleFromGOApi(userId)
            
            // Map to domain model, passing the role directly
            userDto.toDomain(userRole)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user: ${e.message}")
            null
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Obtener todos los usuarios
            val response = api.getUsers()
            if (!response.isSuccessful) {
                android.util.Log.e("appflow UserRepository", "Server error: ${response.code()}")
                return@withContext Result.failure(Exception("Error de servidor. Por favor intenta más tarde"))
            }

            // Log the response for debugging
            android.util.Log.d("appflow UserRepository", "Users response: ${response.body()}")

            // Primero verificar si existe el email
            val userWithEmail = response.body()?.find { it.email == email }
            if (userWithEmail == null) {
                android.util.Log.e("appflow UserRepository", "User not found with email: $email")
                return@withContext Result.failure(Exception("El correo electrónico no está registrado"))
            }

            // Log user found
            android.util.Log.d("appflow UserRepository", "Found user: $userWithEmail")

            // Luego verificar la contraseña
            val user = response.body()?.find { 
                it.email == email && it.password == password 
            }?.toDomain()
            
            if (user == null) {
                android.util.Log.e("appflow UserRepository", "Invalid password for email: $email")
                return@withContext Result.failure(Exception("Contraseña incorrecta"))
            }

            // Verificar si el usuario está activo
            if (!user.isActive) {
                android.util.Log.e("appflow UserRepository", "User account is inactive: $email")
                return@withContext Result.failure(Exception("Tu cuenta está desactivada. Contacta al administrador"))
            }

            // Verificar si el usuario está aprobado
            if (!user.isApproved) {
                android.util.Log.e("appflow UserRepository", "User account is not approved: $email")
                return@withContext Result.failure(Exception("Tu cuenta está pendiente de aprobación. Por favor espera a que un administrador la apruebe."))
            }

            // Update lastLogin timestamp
            val updatedUser = user.copy(
                lastLogin = java.time.Instant.now()
                    .atZone(java.time.ZoneId.systemDefault())
                    .format(java.time.format.DateTimeFormatter.ISO_DATE_TIME)
            )

            // Update user in API
            val updateResponse = api.saveUser( updatedUser.toDto())
            if (!updateResponse.isSuccessful) {
                android.util.Log.e("appflow UserRepository", "Failed to update lastLogin timestamp")
            }

            // Log successful login
            android.util.Log.d("appflow UserRepository", "Successful login for user: ${updatedUser.email}")

            // Guardar usuario en AuthDataStore y actualizar presencia
            authDataStore.setCurrentUser(updatedUser)
            updatePresence(true)
            
            Result.success(updatedUser)
        } catch (e: Exception) {
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
                lastName = lastName
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
                lastName = user.lastName
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
                lastName = user.lastName
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
} 