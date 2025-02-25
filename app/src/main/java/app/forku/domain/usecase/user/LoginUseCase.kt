package app.forku.domain.usecase.user

import app.forku.data.api.dto.user.UserDto
import app.forku.data.repository.user.AuthRepositoryImpl
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke(email: String, password: String): Result<UserDto> {
        return authRepository.login(email, password)
    }
}