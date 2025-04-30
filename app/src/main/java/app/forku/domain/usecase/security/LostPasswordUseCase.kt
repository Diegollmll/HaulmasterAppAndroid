package app.forku.domain.usecase.security

import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class LostPasswordUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> = 
        repository.lostPassword(email)
} 