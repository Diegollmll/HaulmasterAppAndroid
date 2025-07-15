package app.forku.domain.usecase.user

import app.forku.domain.model.user.User
import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val goSecurityProviderRepository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return goSecurityProviderRepository.authenticate(email, password)
    }
}