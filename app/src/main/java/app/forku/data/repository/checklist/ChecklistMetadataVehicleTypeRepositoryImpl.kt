package app.forku.data.repository.checklist

import app.forku.data.api.ChecklistMetadataVehicleTypeApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.checklist.ChecklistMetadataVehicleType
import app.forku.domain.repository.checklist.ChecklistMetadataVehicleTypeRepository
import javax.inject.Inject

class ChecklistMetadataVehicleTypeRepositoryImpl @Inject constructor(
    private val api: ChecklistMetadataVehicleTypeApi
) : ChecklistMetadataVehicleTypeRepository {
    override suspend fun getById(id: String): ChecklistMetadataVehicleType? {
        return api.getById(id).body()?.toDomain()
    }

    override suspend fun getAll(): List<ChecklistMetadataVehicleType> {
        return api.getList().body()?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun save(metadata: ChecklistMetadataVehicleType): ChecklistMetadataVehicleType {
        return api.save(metadata.toDto()).body()?.toDomain()
            ?: throw Exception("Failed to save ChecklistMetadataVehicleType")
    }

    override suspend fun delete(metadata: ChecklistMetadataVehicleType) {
        api.delete(metadata.toDto())
    }
} 