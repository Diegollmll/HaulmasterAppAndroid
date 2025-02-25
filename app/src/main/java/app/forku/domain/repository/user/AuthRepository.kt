package app.forku.domain.repository.user

import app.forku.data.api.dto.user.UserDto
import app.forku.domain.model.user.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserDto>
    suspend fun getCurrentUser(): User?
}