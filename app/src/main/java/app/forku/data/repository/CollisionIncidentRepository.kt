package app.forku.data.repository

import app.forku.data.api.CollisionIncidentApi
import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson

@Singleton
class CollisionIncidentRepository @Inject constructor(
    private val api: CollisionIncidentApi,
    private val gson: Gson
) : ICollisionIncidentRepository {
    override suspend fun getCollisionIncidentById(id: Long): Flow<Result<CollisionIncidentDto>> = flow {
        try {
            val result = api.getById(id)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getCollisionIncidentList(): Flow<Result<List<CollisionIncidentDto>>> = flow {
        try {
            val result = api.getList()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun getCollisionIncidentCount(): Flow<Result<Int>> = flow {
        try {
            val result = api.getCount()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun saveCollisionIncident(
        incident: CollisionIncidentDto,
        include: String?,
        dateformat: String?
    ): Flow<Result<CollisionIncidentDto>> = flow {
        try {
            val entityJson = gson.toJson(incident)
            android.util.Log.d("CollisionIncidentDto", "JSON enviado a API: $entityJson")
            val result = api.save(entity = entityJson, include = include, dateformat = dateformat)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteCollisionIncidentById(id: Long): Flow<Result<Unit>> = flow {
        try {
            api.deleteById(id)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 