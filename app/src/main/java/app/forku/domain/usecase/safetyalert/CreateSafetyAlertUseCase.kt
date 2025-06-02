package app.forku.domain.usecase.safetyalert

import app.forku.data.api.dto.safetyalert.SafetyAlertDto
import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import javax.inject.Inject

class CreateSafetyAlertUseCase @Inject constructor(
    private val repository: SafetyAlertRepository
) {
    suspend operator fun invoke(alert: SafetyAlertDto): SafetyAlertDto? {
        return repository.saveSafetyAlert(alert)
    }
} 