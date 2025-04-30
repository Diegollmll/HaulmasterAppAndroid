package app.forku.domain.usecase.security

import app.forku.domain.model.user.User
import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class RegisterByEmailUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<User> = repository.registerByEmail(email, password, firstName, lastName)
} 