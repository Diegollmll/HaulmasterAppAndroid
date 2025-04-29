package app.forku.domain.repository

interface IGOServicesRepository {
    suspend fun getCsrfTokenAndCookie(): Result<Pair<String?, String?>>
    suspend fun getStoredCsrfToken(): String?
    suspend fun clearCsrfToken()
} 