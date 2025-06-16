package app.forku.data.repository

import app.forku.data.api.CollisionIncidentApi
import app.forku.data.dto.CollisionIncidentDto
import app.forku.domain.repository.ICollisionIncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager

@Singleton
class CollisionIncidentRepository @Inject constructor(
    private val api: CollisionIncidentApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
) : ICollisionIncidentRepository {
    override suspend fun getCollisionIncidentById(id: String): Flow<Result<CollisionIncidentDto>> = flow {
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
            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("CollisionIncidentRepo", "=== COLLISION INCIDENT REPOSITORY DEBUG ===")
            android.util.Log.d("CollisionIncidentRepo", "Original DTO businessId: '${incident.businessId}', siteId: '${incident.siteId}'")
            android.util.Log.d("CollisionIncidentRepo", "BusinessId from BusinessContextManager: '$businessId'")
            android.util.Log.d("CollisionIncidentRepo", "SiteId from BusinessContextManager: '$siteId'")
            
            // Assign businessId and siteId to DTO copy
            val incidentWithContext = incident.copy(
                businessId = businessId,
                siteId = siteId
            )
            android.util.Log.d("CollisionIncidentRepo", "Updated DTO businessId: '${incidentWithContext.businessId}', siteId: '${incidentWithContext.siteId}'")
            
            val entityJson = gson.toJson(incidentWithContext)
            android.util.Log.d("CollisionIncidentRepo", "JSON enviado a API: $entityJson")
            android.util.Log.d("CollisionIncidentRepo", "Calling API with businessId: '$businessId', siteId: '$siteId'")
            
            val result = api.save(
                entity = entityJson, 
                include = include, 
                dateformat = dateformat, 
                businessId = businessId
            )
            android.util.Log.d("CollisionIncidentRepo", "API response received successfully")
            android.util.Log.d("CollisionIncidentRepo", "=========================================")
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("CollisionIncidentRepo", "Error saving collision incident: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteCollisionIncidentById(id: String): Flow<Result<Unit>> = flow {
        try {
            api.deleteById(id)
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
} 