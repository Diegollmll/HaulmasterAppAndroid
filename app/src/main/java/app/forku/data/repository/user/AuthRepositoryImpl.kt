package app.forku.data.repository.user

import app.forku.data.api.Sub7Api
import app.forku.data.api.dto.user.LoginRequestDto
import app.forku.data.api.dto.user.UserDto
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.domain.model.user.User
import app.forku.domain.repository.user.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val authDataStore: AuthDataStore
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = api.login(LoginRequestDto(email, password))
            if (response.isSuccessful) {
                response.body()?.let { user ->
                    authDataStore.setCurrentUser(user.toDomain())
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return authDataStore.getCurrentUser()
    }

    override suspend fun refreshCurrentUser(): User? {
        return try {
            val currentUser = authDataStore.getCurrentUser() ?: return null
            val response = api.getUser(currentUser.id)
            
            if (response.isSuccessful) {
                response.body()?.let { userDto ->
                    val updatedUser = userDto.toDomain()
                    authDataStore.setCurrentUser(updatedUser)
                    updatedUser
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}