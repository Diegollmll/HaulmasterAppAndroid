package app.forku.domain.usecase

import app.forku.domain.repository.IGOServicesRepository
import javax.inject.Inject

class GetCsrfTokenUseCase @Inject constructor(
    private val repository: IGOServicesRepository
) {
    suspend operator fun invoke(): Result<String> {
        return repository.getCsrfToken()
    }
} 