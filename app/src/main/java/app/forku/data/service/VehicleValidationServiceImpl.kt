package app.forku.data.service

import app.forku.domain.model.session.VehicleSessionStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.getErrorMessage
import app.forku.domain.model.vehicle.isAvailable
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.session.SessionStatusChecker
import app.forku.domain.service.VehicleValidationService
import app.forku.domain.service.VehicleStatusDeterminer
import javax.inject.Inject

class VehicleValidationServiceImpl @Inject constructor(
    private val sessionStatusChecker: SessionStatusChecker,
    private val checklistRepository: ChecklistRepository,
    private val vehicleStatusDeterminer: VehicleStatusDeterminer
) : VehicleValidationService {
    
    override suspend fun getVehicleStatus(vehicleId: String, businessId: String): VehicleStatus {
        // First check if there's an active session
        val activeSession = sessionStatusChecker.getActiveSessionForVehicle(vehicleId, businessId)
        if (activeSession?.status == VehicleSessionStatus.OPERATING) {
            return VehicleStatus.IN_USE
        }
        
        // Get the last check and determine status
        val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId, businessId)
        return vehicleStatusDeterminer.determineStatusFromCheck(checkStatus = lastCheck?.status ?: "")
    }

    override suspend fun isVehicleAvailable(vehicleId: String, businessId: String): Boolean {
        return getVehicleStatus(vehicleId, businessId) == VehicleStatus.AVAILABLE
    }

    override suspend fun getVehicleErrorMessage(vehicleId: String, businessId: String): String? {
        val status = getVehicleStatus(vehicleId, businessId)
        return if (status.isAvailable()) null else status.getErrorMessage()
    }

    override suspend fun validateVehicleForOperation(vehicleId: String, businessId: String) {
        val status = getVehicleStatus(vehicleId, businessId)
        if (!status.isAvailable()) {
            throw Exception(status.getErrorMessage())
        }
    }

    override fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return vehicleStatusDeterminer.determineStatusFromCheck(checkStatus)
    }
} 