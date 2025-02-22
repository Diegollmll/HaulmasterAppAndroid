package app.forku.data.repository

import app.forku.data.api.Sub7Api
import app.forku.data.api.dto.LoginRequestDto
import app.forku.data.api.dto.LoginResponseDto
import app.forku.data.api.dto.RegisterRequestDto
import app.forku.data.api.dto.RegisterResponseDto
import app.forku.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: Sub7Api
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Result<LoginResponseDto> {
        return try {
            val response = api.login(LoginRequestDto(username, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}