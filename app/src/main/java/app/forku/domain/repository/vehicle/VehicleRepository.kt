package app.forku.domain.repository.vehicle


import app.forku.domain.model.checklist.Answer
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.PreShiftCheck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay


interface VehicleRepository {
    suspend fun getVehicle(id: String): Vehicle
    suspend fun getVehicles(): List<Vehicle>
    suspend fun getVehicleByQr(code: String): Vehicle
    suspend fun getChecklistItems(vehicleId: String): List<Checklist>
    suspend fun submitPreShiftCheck(
        vehicleId: String,
        checkItems: List<ChecklistItem>,
        checkId: String?
    ): PreShiftCheck
    suspend fun getLastPreShiftCheck(vehicleId: String): PreShiftCheck?
    suspend fun submitChecklist(vehicleId: String, answers: List<Answer>): PreShiftCheck
    suspend fun getChecklist(vehicleId: String): Checklist
}