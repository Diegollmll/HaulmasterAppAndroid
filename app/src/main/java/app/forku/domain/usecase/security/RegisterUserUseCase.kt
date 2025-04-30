package app.forku.domain.usecase.security

import app.forku.domain.model.user.User
import app.forku.domain.repository.IGOSecurityProviderRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repository: IGOSecurityProviderRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<User> = repository.register(email, password, firstName, lastName)
} 