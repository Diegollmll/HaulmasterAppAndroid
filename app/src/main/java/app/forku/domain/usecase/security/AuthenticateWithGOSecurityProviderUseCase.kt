package app.forku.domain.usecase.security

import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class AuthenticateWithGOSecurityProviderUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<String> {
        return repository.authenticate(username, password)
    }
} 