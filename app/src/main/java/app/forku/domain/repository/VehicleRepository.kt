package app.forku.domain.repository

import app.forku.domain.model.Vehicle
import app.forku.domain.model.Checklist
import app.forku.domain.model.ChecklistItem

interface VehicleRepository {
    suspend fun getVehicles(): List<Vehicle>
    suspend fun getVehicleById(id: String): Vehicle
    suspend fun getVehicleByQr(code: String): Vehicle
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun submitPreShiftCheck(vehicleId: String, checkItems: List<ChecklistItem>): Boolean
}