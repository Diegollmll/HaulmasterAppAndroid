package app.forku.domain.repository.incident

import app.forku.data.dto.VehicleFailIncidentDto
import kotlinx.coroutines.flow.Flow

interface VehicleFailIncidentRepository {
    suspend fun getById(id: String): Result<VehicleFailIncidentDto>
    suspend fun getList(): Result<List<VehicleFailIncidentDto>>
    suspend fun getCount(): Result<Int>
    suspend fun save(entity: String, include: String? = null, dateformat: String? = "ISO8601"): Flow<Result<VehicleFailIncidentDto>>
    suspend fun deleteById(id: String): Result<Unit>
} 