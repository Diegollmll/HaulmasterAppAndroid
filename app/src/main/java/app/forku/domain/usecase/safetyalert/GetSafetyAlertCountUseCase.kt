package app.forku.domain.usecase.safetyalert

import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import javax.inject.Inject

class GetSafetyAlertCountUseCase @Inject constructor(
    private val repository: SafetyAlertRepository
) {
    suspend operator fun invoke(): Int {
        return repository.getSafetyAlertCount()
    }
} 