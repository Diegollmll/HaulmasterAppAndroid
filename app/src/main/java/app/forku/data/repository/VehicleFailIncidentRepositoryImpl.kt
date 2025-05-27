package app.forku.data.repository

import app.forku.data.api.VehicleFailIncidentApi
import app.forku.data.dto.VehicleFailIncidentDto
import app.forku.domain.repository.incident.VehicleFailIncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class VehicleFailIncidentRepositoryImpl @Inject constructor(
    private val api: VehicleFailIncidentApi
) : VehicleFailIncidentRepository {

    override suspend fun getById(id: String): Result<VehicleFailIncidentDto> = runCatching {
        api.getById(id)
    }

    override suspend fun getList(): Result<List<VehicleFailIncidentDto>> = runCatching {
        api.getList()
    }

    override suspend fun getCount(): Result<Int> = runCatching {
        api.getCount()
    }

    override suspend fun save(
        entity: String,
        include: String?,
        dateformat: String?
    ): Flow<Result<VehicleFailIncidentDto>> = flow {
        try {
            val result = api.save(entity, include, dateformat)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteById(id: String): Result<Unit> = runCatching {
        api.deleteById(id)
    }
} 