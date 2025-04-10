package app.forku.domain.repository.vehicle

import app.forku.domain.model.vehicle.VehicleCategory

interface VehicleCategoryRepository {
    suspend fun getVehicleCategories(): List<VehicleCategory>
    suspend fun getVehicleCategory(id: String): VehicleCategory
    suspend fun createVehicleCategory(name: String, description: String? = null): VehicleCategory
    suspend fun updateVehicleCategory(id: String, name: String, description: String? = null): VehicleCategory
    suspend fun deleteVehicleCategory(id: String)
} 