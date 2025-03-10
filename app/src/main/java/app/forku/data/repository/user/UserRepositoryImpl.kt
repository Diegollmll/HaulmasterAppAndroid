package app.forku.data.repository.user

import app.forku.data.api.GeneralApi
import app.forku.data.api.dto.user.UserDto
import app.forku.data.mapper.toDomain
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import app.forku.data.datastore.AuthDataStore
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton
import app.forku.domain.model.user.UserRole

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: GeneralApi,
    private val authDataStore: AuthDataStore
) : UserRepository {
    
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

            // Crear nuevo usuario
            val newUser = UserDto(
                id = UUID.randomUUID().toString(),
                email = email,
                password = password,
                username = email,  // Usando email como username
                name = "$firstName $lastName",
                token = UUID.randomUUID().toString(),
                refreshToken = UUID.randomUUID().toString(),
                photoUrl = null,
                role = UserRole.USER.name,
                permissions = listOf(),
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
} 