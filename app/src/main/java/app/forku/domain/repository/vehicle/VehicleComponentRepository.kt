package app.forku.domain.repository.vehicle

import app.forku.data.api.dto.vehicle.VehicleComponentDto
import retrofit2.Response

interface VehicleComponentRepository {
    suspend fun getAllComponents(): Response<List<VehicleComponentDto>>
    suspend fun getComponentById(id: String): Response<VehicleComponentDto>
    suspend fun createComponent(component: VehicleComponentDto): Response<VehicleComponentDto>
    suspend fun updateComponent(id: String, component: VehicleComponentDto): Response<VehicleComponentDto>
    suspend fun deleteComponent(id: String): Response<Unit>
} 