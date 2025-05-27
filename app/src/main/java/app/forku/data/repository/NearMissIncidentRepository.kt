package app.forku.data.repository

import app.forku.data.api.NearMissIncidentApi
import app.forku.data.dto.NearMissIncidentDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson

@Singleton
class NearMissIncidentRepository @Inject constructor(
    private val api: NearMissIncidentApi,
    private val gson: Gson
) {
    suspend fun getNearMissIncidentById(id: String): Flow<Result<NearMissIncidentDto>> = flow {
        try {
            val result = api.getById(id)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getNearMissIncidentList(): Flow<Result<List<NearMissIncidentDto>>> = flow {
        try {
            val result = api.getList()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getNearMissIncidentCount(): Flow<Result<Int>> = flow {
        try {
            val result = api.getCount()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun saveNearMissIncident(incident: NearMissIncidentDto): Flow<Result<NearMissIncidentDto>> = flow {
        try {
            val entityJson = gson.toJson(incident)
            android.util.Log.d("NearMissIncidentDto", "JSON enviado a API: $entityJson")
            val result = api.save(entity = entityJson)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun deleteNearMissIncidentById(id: String): Flow<Result<Unit>> = flow {
        try {
            api.deleteById(id)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 