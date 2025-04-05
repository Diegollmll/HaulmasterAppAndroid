package app.forku.domain.usecase.user

import app.forku.domain.model.user.User
import app.forku.domain.repository.user.UserRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): User? {
        return userRepository.getCurrentUser()
    }
} 