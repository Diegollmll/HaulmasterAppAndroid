package app.forku.domain.repository.safetyalert

import app.forku.data.api.dto.safetyalert.SafetyAlertDto

interface SafetyAlertRepository {
    suspend fun getSafetyAlertById(id: String): SafetyAlertDto?
    suspend fun getSafetyAlertList(): List<SafetyAlertDto>
    suspend fun saveSafetyAlert(alert: SafetyAlertDto): SafetyAlertDto?
    suspend fun deleteSafetyAlert(alert: SafetyAlertDto): Boolean
    suspend fun getSafetyAlertCount(businessId: String? = null, siteId: String? = null): Int
} 