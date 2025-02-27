package app.forku.domain.repository.user

import app.forku.domain.model.user.User

interface UserRepository {
    suspend fun getUserById(id: String): User?
} 