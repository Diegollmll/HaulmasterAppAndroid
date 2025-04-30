package app.forku.domain.usecase.security

import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(oldPassword: String, newPassword: String): Result<Unit> =
        repository.changePassword(oldPassword, newPassword)
} 