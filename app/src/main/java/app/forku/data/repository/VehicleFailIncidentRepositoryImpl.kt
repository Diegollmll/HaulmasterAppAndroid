package app.forku.data.repository

import app.forku.data.api.VehicleFailIncidentApi
import app.forku.data.dto.VehicleFailIncidentDto
import app.forku.domain.repository.incident.VehicleFailIncidentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager

class VehicleFailIncidentRepositoryImpl @Inject constructor(
    private val api: VehicleFailIncidentApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
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
            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("VehicleFailIncidentRepo", "=== VEHICLE FAIL INCIDENT REPOSITORY DEBUG ===")
            android.util.Log.d("VehicleFailIncidentRepo", "Original entity JSON: $entity")
            android.util.Log.d("VehicleFailIncidentRepo", "BusinessId from BusinessContextManager: '$businessId'")
            android.util.Log.d("VehicleFailIncidentRepo", "SiteId from BusinessContextManager: '$siteId'")
            
            // Parse existing entity to add businessId and siteId
            val incidentDto = gson.fromJson(entity, VehicleFailIncidentDto::class.java)
            val incidentWithContext = incidentDto.copy(
                businessId = businessId,
                siteId = siteId
            )
            android.util.Log.d("VehicleFailIncidentRepo", "Updated DTO businessId: '${incidentWithContext.businessId}', siteId: '${incidentWithContext.siteId}'")
            
            val entityJson = gson.toJson(incidentWithContext)
            android.util.Log.d("VehicleFailIncidentRepo", "JSON enviado a API: $entityJson")
            android.util.Log.d("VehicleFailIncidentRepo", "Calling API with businessId: '$businessId', siteId: '$siteId'")
            
            val result = api.save(
                entity = entityJson, 
                include = include, 
                dateformat = dateformat,
                businessId = businessId
            )
            android.util.Log.d("VehicleFailIncidentRepo", "API response received successfully")
            android.util.Log.d("VehicleFailIncidentRepo", "============================================")
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("VehicleFailIncidentRepo", "Error saving vehicle fail incident: ${e.message}", e)
            emit(Result.failure(e))
        }
    }

    override suspend fun deleteById(id: String): Result<Unit> = runCatching {
        api.deleteById(id)
    }
} 