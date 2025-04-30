package app.forku.domain.usecase.security

import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> =
        repository.resetPassword(token, newPassword)
} 