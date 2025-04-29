package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistMetadataVehicleType

interface ChecklistMetadataVehicleTypeRepository {
    suspend fun getById(id: String): ChecklistMetadataVehicleType?
    suspend fun getAll(): List<ChecklistMetadataVehicleType>
    suspend fun save(metadata: ChecklistMetadataVehicleType): ChecklistMetadataVehicleType
    suspend fun delete(metadata: ChecklistMetadataVehicleType)
} 