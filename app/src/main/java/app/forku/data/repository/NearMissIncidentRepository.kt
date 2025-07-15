package app.forku.data.repository

import app.forku.data.api.NearMissIncidentApi
import app.forku.data.dto.NearMissIncidentDto
import app.forku.core.utils.safeEmitFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager

@Singleton
class NearMissIncidentRepository @Inject constructor(
    private val api: NearMissIncidentApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
) {
    suspend fun getNearMissIncidentById(id: String): Flow<Result<NearMissIncidentDto>> = flow {
        try {
            val result = api.getById(id)
            emit(Result.success(result))
        } catch (e: Exception) {
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    suspend fun getNearMissIncidentList(): Flow<Result<List<NearMissIncidentDto>>> = flow {
        try {
            val result = api.getList()
            emit(Result.success(result))
        } catch (e: Exception) {
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    suspend fun getNearMissIncidentCount(): Flow<Result<Int>> = flow {
        try {
            val result = api.getCount()
            emit(Result.success(result))
        } catch (e: Exception) {
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    suspend fun saveNearMissIncident(incident: NearMissIncidentDto): Flow<Result<NearMissIncidentDto>> = flow {
        try {
            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("NearMissIncidentRepo", "=== NEAR MISS INCIDENT REPOSITORY DEBUG ===")
            android.util.Log.d("NearMissIncidentRepo", "Original DTO businessId: '${incident.businessId}', siteId: '${incident.siteId}'")
            android.util.Log.d("NearMissIncidentRepo", "BusinessId from BusinessContextManager: '$businessId'")
            android.util.Log.d("NearMissIncidentRepo", "SiteId from BusinessContextManager: '$siteId'")
            
            // Assign businessId and siteId to DTO copy
            val incidentWithContext = incident.copy(
                businessId = businessId,
                siteId = siteId
            )
            android.util.Log.d("NearMissIncidentRepo", "Updated DTO businessId: '${incidentWithContext.businessId}', siteId: '${incidentWithContext.siteId}'")
            
            val entityJson = gson.toJson(incidentWithContext)
            android.util.Log.d("NearMissIncidentRepo", "JSON enviado a API: $entityJson")
            android.util.Log.d("NearMissIncidentRepo", "Calling API with businessId: '$businessId', siteId: '$siteId'")
            
            val result = api.save(
                entity = entityJson, 
                businessId = businessId
            )
            android.util.Log.d("NearMissIncidentRepo", "API response received successfully")
            android.util.Log.d("NearMissIncidentRepo", "==========================================")
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("NearMissIncidentRepo", "Error saving near miss incident: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }

    suspend fun deleteNearMissIncidentById(id: String): Flow<Result<Unit>> = flow {
        try {
            api.deleteById(id)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }
} 