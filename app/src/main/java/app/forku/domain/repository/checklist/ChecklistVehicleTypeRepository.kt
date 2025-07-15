package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistVehicleType

interface ChecklistVehicleTypeRepository {
    suspend fun getVehicleTypesByChecklistId(checklistId: String): List<ChecklistVehicleType>
    suspend fun getAllVehicleTypes(): List<ChecklistVehicleType>
    suspend fun getVehicleTypeById(id: String): ChecklistVehicleType?
    suspend fun saveVehicleTypeAssociation(vehicleType: ChecklistVehicleType): ChecklistVehicleType
    suspend fun deleteVehicleTypeAssociation(id: String): Boolean
} 