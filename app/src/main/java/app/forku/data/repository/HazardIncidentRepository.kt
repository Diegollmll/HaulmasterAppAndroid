package app.forku.data.repository

import app.forku.data.api.HazardIncidentApi
import app.forku.data.dto.HazardIncidentDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson

@Singleton
class HazardIncidentRepository @Inject constructor(
    private val api: HazardIncidentApi,
    private val gson: Gson
) {
    suspend fun saveHazardIncident(
        incident: HazardIncidentDto,
        include: String? = null,
        dateformat: String? = "ISO8601"
    ): Flow<Result<HazardIncidentDto>> = flow {
        try {
            val entityJson = gson.toJson(incident)
            val result = api.save(entity = entityJson, include = include, dateformat = dateformat)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    // Puedes agregar métodos getById, getList, etc. si los necesitas
} 