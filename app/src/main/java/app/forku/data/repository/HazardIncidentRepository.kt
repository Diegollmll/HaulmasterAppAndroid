package app.forku.data.repository

import app.forku.data.api.HazardIncidentApi
import app.forku.data.dto.HazardIncidentDto
import app.forku.core.utils.safeEmitFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager

@Singleton
class HazardIncidentRepository @Inject constructor(
    private val api: HazardIncidentApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
) {
    suspend fun saveHazardIncident(
        incident: HazardIncidentDto,
        include: String? = null,
        dateformat: String? = "ISO8601"
    ): Flow<Result<HazardIncidentDto>> = flow {
        try {
            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("HazardIncidentRepo", "=== HAZARD INCIDENT REPOSITORY DEBUG ===")
            android.util.Log.d("HazardIncidentRepo", "Original DTO businessId: '${incident.businessId}', siteId: '${incident.siteId}'")
            android.util.Log.d("HazardIncidentRepo", "BusinessId from BusinessContextManager: '$businessId'")
            android.util.Log.d("HazardIncidentRepo", "SiteId from BusinessContextManager: '$siteId'")
            
            // Assign businessId and siteId to DTO copy
            val incidentWithContext = incident.copy(
                businessId = businessId,
                siteId = siteId
            )
            android.util.Log.d("HazardIncidentRepo", "Updated DTO businessId: '${incidentWithContext.businessId}', siteId: '${incidentWithContext.siteId}'")
            
            val entityJson = gson.toJson(incidentWithContext)
            android.util.Log.d("HazardIncidentRepo", "JSON enviado a API: $entityJson")
            android.util.Log.d("HazardIncidentRepo", "Calling API with businessId: '$businessId', siteId: '$siteId'")
            
            val result = api.save(
                entity = entityJson, 
                include = include, 
                dateformat = dateformat, 
                businessId = businessId
            )
            android.util.Log.d("HazardIncidentRepo", "API response received successfully")
            android.util.Log.d("HazardIncidentRepo", "======================================")
            emit(Result.success(result))
        } catch (e: Exception) {
            android.util.Log.e("HazardIncidentRepo", "Error saving hazard incident: ${e.message}", e)
            safeEmitFailure(e) { failure -> emit(failure) }
        }
    }
    // You can add getById, getList, etc. methods if you need them
} 