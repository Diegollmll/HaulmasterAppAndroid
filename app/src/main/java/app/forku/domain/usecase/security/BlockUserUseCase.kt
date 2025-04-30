package app.forku.domain.usecase.security

import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        repository.blockUser(userId)
} 