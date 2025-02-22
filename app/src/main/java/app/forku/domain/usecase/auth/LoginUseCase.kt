package app.forku.domain.usecase.auth

import app.forku.data.api.dto.LoginResponseDto
import app.forku.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImpl
) {
    suspend operator fun invoke(username: String, password: String): Result<LoginResponseDto> {
        return authRepository.login(username, password)
    }
}