package app.forku.domain.repository

import app.forku.data.api.dto.LoginResponseDto

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResponseDto>
    //suspend fun register(username: String, password: String): Result<RegisterResponseDto>
}