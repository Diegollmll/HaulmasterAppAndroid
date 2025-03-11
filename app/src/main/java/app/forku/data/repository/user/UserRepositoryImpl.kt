package app.forku.data.repository.user

import app.forku.data.api.GeneralApi
import app.forku.data.api.dto.user.UserDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDto
import app.forku.domain.model.user.Permissions
import app.forku.domain.model.user.UserRole
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: GeneralApi,
    private val authDataStore: AuthDataStore,
    private val sharedPreferences: SharedPreferences
) : UserRepository {
    
    companion object {
        private const val PREF_TOUR_COMPLETED = "tour_completed"
    }

    override suspend fun getUserById(id: String): User? {
        return try {
            val response = api.getUser(id)
            if (!response.isSuccessful) return null
            response.body()?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            // Obtener todos los usuarios
            val response = api.getUsers()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to fetch users"))
            }

            // Buscar usuario por email y password
            val user = response.body()?.find { 
                it.email == email && it.password == password 
            }?.toDomain() ?: return@withContext Result.failure(Exception("Invalid credentials"))

            // Guardar usuario en AuthDataStore
            authDataStore.setCurrentUser(user)
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
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

            // Por defecto, los nuevos usuarios se registran con rol USER
            val role = UserRole.USER
            val permissions = when (role) {
                UserRole.ADMIN -> Permissions.ADMIN_PERMISSIONS
                UserRole.OPERATOR -> Permissions.OPERATOR_PERMISSIONS
                UserRole.USER -> Permissions.USER_PERMISSIONS
            }

            // Crear nuevo usuario
            val newUser = UserDto(
                id = UUID.randomUUID().toString(),
                email = email,
                password = password,
                username = email,
                name = "$firstName $lastName",
                token = UUID.randomUUID().toString(),
                refreshToken = UUID.randomUUID().toString(),
                photoUrl = null,
                role = role.name,
                permissions = permissions.toList(),
                certifications = listOf(),
                last_medical_check = null,
                last_login = null,
                is_active = true
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
        authDataStore.clearAuth()
    }

    override suspend fun updateUserRole(userId: String, newRole: UserRole): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            
            // Asignar permisos según el nuevo rol
            val newPermissions = when (newRole) {
                UserRole.ADMIN -> Permissions.ADMIN_PERMISSIONS
                UserRole.OPERATOR -> Permissions.OPERATOR_PERMISSIONS
                UserRole.USER -> Permissions.USER_PERMISSIONS
            }

            val updatedUserDto = UserDto(
                id = user.id,
                email = user.email,
                password = "", // No incluimos el password en la actualización
                username = user.username,
                name = user.name,
                token = user.token,
                refreshToken = user.refreshToken,
                photoUrl = user.photoUrl,
                role = newRole.name,
                permissions = newPermissions.toList(),
                certifications = user.certifications.map { it.toDto() },
                last_medical_check = user.lastMedicalCheck,
                last_login = user.lastLogin,
                is_active = true
            )

            val response = api.updateUser(userId, updatedUserDto)
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
            val userDto = UserDto(
                id = user.id,
                email = user.email,
                password = "", // No incluimos el password en la actualización
                username = user.username,
                name = user.name,
                token = user.token,
                refreshToken = user.refreshToken,
                photoUrl = user.photoUrl,
                role = user.role.name,
                permissions = user.permissions,
                certifications = user.certifications.map { it.toDto() },
                last_medical_check = user.lastMedicalCheck,
                last_login = user.lastLogin,
                is_active = user.isActive
            )

            val response = api.updateUser(user.id, userDto)
            if (!response.isSuccessful) {
                throw Exception("Failed to update user")
            }

            // Si el usuario actualizado es el usuario actual, actualizar en AuthDataStore
            getCurrentUser()?.let { currentUser ->
                if (currentUser.id == user.id) {
                    authDataStore.setCurrentUser(user)
                }
            }
        } catch (e: Exception) {
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
                ?.filter { it.role.uppercase() == role.name }
                ?.map { it.toDomain() }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getUsers()
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            response.body()?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun deleteUser(userId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun searchUsers(query: String): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun observeCurrentUser(): Flow<User?> {
        TODO("Not yet implemented")
    }

    override suspend fun getTourCompletionStatus(): Boolean {
        return sharedPreferences.getBoolean(PREF_TOUR_COMPLETED, false)
    }

    override suspend fun setTourCompleted() {
        sharedPreferences.edit().putBoolean(PREF_TOUR_COMPLETED, true).apply()
    }

    override suspend fun getAuthToken(): String? {
        return authDataStore.getCurrentUser()?.token
    }
} 