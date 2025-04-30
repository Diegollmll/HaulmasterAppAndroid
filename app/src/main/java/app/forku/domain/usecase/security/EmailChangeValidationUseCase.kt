package app.forku.domain.usecase.security

import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class EmailChangeValidationUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend fun validateEmailChange(token: String): Result<Unit> =
        repository.validateEmailChange(token)

    suspend fun resendValidationEmail(): Result<Unit> =
        repository.resendEmailChangeValidation()

    suspend fun cancelEmailChange(): Result<Unit> =
        repository.cancelEmailChange()
} 