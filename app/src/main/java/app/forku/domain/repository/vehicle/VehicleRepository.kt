package app.forku.domain.repository.vehicle

import VehicleSession
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck


interface VehicleRepository {
    suspend fun getVehicles(): List<Vehicle>
    suspend fun getVehicleById(id: String): Vehicle
    suspend fun getVehicleByQr(code: String): Vehicle
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun submitPreShiftCheck(vehicleId: String, checkItems: List<ChecklistItem>): Boolean
    suspend fun getLastPreShiftCheck(): PreShiftCheck?
    suspend fun startSession(vehicleId: String, checkId: String): VehicleSession
    suspend fun endSession(sessionId: String): VehicleSession
    suspend fun getCurrentSession(): VehicleSession?
}