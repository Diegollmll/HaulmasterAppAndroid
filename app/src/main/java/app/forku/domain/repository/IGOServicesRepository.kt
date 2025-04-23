package app.forku.domain.repository

interface IGOServicesRepository {
    suspend fun getCsrfToken(): Result<String>
    suspend fun getStoredCsrfToken(): String?
    suspend fun clearCsrfToken()
} 