package app.forku.domain.repository

interface IGOSecurityProviderRepository {
    suspend fun authenticate(username: String, password: String): Result<String>
    suspend fun logout()
} 