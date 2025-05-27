package app.forku.domain.repository

import app.forku.data.dto.CollisionIncidentDto
import kotlinx.coroutines.flow.Flow

interface ICollisionIncidentRepository {
    suspend fun getCollisionIncidentById(id: Long): Flow<Result<CollisionIncidentDto>>
    suspend fun getCollisionIncidentList(): Flow<Result<List<CollisionIncidentDto>>>
    suspend fun saveCollisionIncident(
        incident: CollisionIncidentDto,
        include: String? = null,
        dateformat: String? = "ISO8601"
    ): Flow<Result<CollisionIncidentDto>>
    suspend fun deleteCollisionIncidentById(id: Long): Flow<Result<Unit>>
    suspend fun getCollisionIncidentCount(): Flow<Result<Int>>
} 