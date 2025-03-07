package app.forku.data.service

import app.forku.data.api.Sub7Api
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.session.SessionStatusChecker
import app.forku.domain.service.VehicleValidationService
import app.forku.domain.service.VehicleStatusDeterminer
import javax.inject.Inject

class VehicleValidationServiceImpl @Inject constructor(
    private val api: Sub7Api,
    private val sessionStatusChecker: SessionStatusChecker,
    private val checklistRepository: ChecklistRepository,
    private val vehicleStatusDeterminer: VehicleStatusDeterminer
) : VehicleValidationService {
    
    override suspend fun getVehicleStatus(vehicleId: String): VehicleStatus {
        // First check if there's an active session
        val activeSession = sessionStatusChecker.getActiveSessionForVehicle(vehicleId)
        if (activeSession?.status == SessionStatus.ACTIVE) {
            return VehicleStatus.IN_USE
        }
        
        // Get the last check and determine status
        val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
        return vehicleStatusDeterminer.determineStatusFromCheck(lastCheck?.status ?: "")
    }

    override suspend fun isVehicleAvailable(vehicleId: String): Boolean {
        return getVehicleStatus(vehicleId) == VehicleStatus.AVAILABLE
    }

    override suspend fun getVehicleErrorMessage(vehicleId: String): String? {
        val status = getVehicleStatus(vehicleId)
        return if (status.isAvailable()) null else status.getErrorMessage()
    }

    override suspend fun validateVehicleForOperation(vehicleId: String) {
        val status = getVehicleStatus(vehicleId)
        if (!status.isAvailable()) {
            throw Exception(status.getErrorMessage())
        }
    }

    override fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return vehicleStatusDeterminer.determineStatusFromCheck(checkStatus)
    }
} 