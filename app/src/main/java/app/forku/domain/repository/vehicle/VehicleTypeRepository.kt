package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.VehicleType

interface VehicleTypeRepository {
    suspend fun getVehicleTypes(): List<VehicleType>
    suspend fun getVehicleTypeById(id: String): VehicleType
    suspend fun getVehicleTypesByCategory(categoryId: String): List<VehicleType>
    suspend fun createVehicleType(name: String, categoryId: String, requiresCertification: Boolean = false): VehicleType
    suspend fun updateVehicleType(id: String, name: String, categoryId: String, requiresCertification: Boolean): VehicleType
    suspend fun deleteVehicleType(id: String)
} 