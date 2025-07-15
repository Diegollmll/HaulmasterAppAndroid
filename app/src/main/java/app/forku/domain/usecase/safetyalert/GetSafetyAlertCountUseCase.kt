package app.forku.domain.usecase.safetyalert

import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import javax.inject.Inject

class GetSafetyAlertCountUseCase @Inject constructor(
    private val repository: SafetyAlertRepository
) {
    suspend operator fun invoke(businessId: String? = null, siteId: String? = null): Int {
        return repository.getSafetyAlertCount(businessId, siteId)
    }
} 