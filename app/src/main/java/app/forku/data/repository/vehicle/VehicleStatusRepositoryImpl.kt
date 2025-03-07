package app.forku.data.repository.vehicle

import app.forku.data.api.Sub7Api
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.session.SessionStatus
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.session.SessionStatusChecker
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import javax.inject.Inject

class VehicleStatusRepositoryImpl @Inject constructor(
    private val api: Sub7Api,
    private val sessionStatusChecker: SessionStatusChecker,
    private val checklistRepository: ChecklistRepository,
    private val vehicleStatusUpdater: VehicleStatusUpdater
) : VehicleStatusRepository {
    override suspend fun getVehicleStatus(vehicleId: String): VehicleStatus {
        return try {
            // First check if there's an active session
            val activeSession = sessionStatusChecker.getActiveSessionForVehicle(vehicleId)
            if (activeSession?.status == SessionStatus.ACTIVE) {
                return VehicleStatus.IN_USE
            }
            
            // Get the last check
            val lastCheck = checklistRepository.getLastPreShiftCheck(vehicleId)
            
            // If there's no last check and no active session, check if we're in checklist creation
            if (lastCheck == null && activeSession == null) {
                // We could add a new method to check if there's a checklist being created
                val isChecklistBeingCreated = checklistRepository.hasChecklistInCreation(vehicleId)
                if (isChecklistBeingCreated) {
                    return VehicleStatus.AVAILABLE
                }
            }
            
            return determineStatusFromCheck(lastCheck?.status ?: "")
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error getting vehicle status", e)
            VehicleStatus.AVAILABLE
        }
    }

    override suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus): Boolean {
        return vehicleStatusUpdater.updateVehicleStatus(vehicleId, status)
    }

    override suspend fun determineStatusFromCheck(checkStatus: String): VehicleStatus {
        return when (checkStatus) {
            CheckStatus.COMPLETED_PASS.toString() -> VehicleStatus.AVAILABLE
            CheckStatus.COMPLETED_FAIL.toString() -> VehicleStatus.OUT_OF_SERVICE
            CheckStatus.IN_PROGRESS.toString() -> VehicleStatus.AVAILABLE
            CheckStatus.EXPIRED.toString() -> VehicleStatus.AVAILABLE
            else -> VehicleStatus.AVAILABLE
        }
    }
} 