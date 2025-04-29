package app.forku.domain.repository

import app.forku.domain.model.user.User

interface IGOSecurityProviderRepository {
    suspend fun authenticate(username: String, password: String): Result<User>
    suspend fun logout()
} 