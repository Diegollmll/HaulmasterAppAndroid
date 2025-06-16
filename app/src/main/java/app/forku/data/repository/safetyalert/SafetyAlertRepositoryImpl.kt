package app.forku.data.repository.safetyalert

import app.forku.data.api.SafetyAlertApi
import app.forku.data.api.dto.safetyalert.SafetyAlertDto
import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import app.forku.core.auth.HeaderManager
import app.forku.core.business.BusinessContextManager
import com.google.gson.Gson
import javax.inject.Inject

class SafetyAlertRepositoryImpl @Inject constructor(
    private val api: SafetyAlertApi,
    private val headerManager: HeaderManager,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager
) : SafetyAlertRepository {
    override suspend fun getSafetyAlertById(id: String): SafetyAlertDto? {
        return try {
            val headers = headerManager.getHeaders().getOrThrow()
            val response = api.getSafetyAlertById(
                id = id,
                csrfToken = headers.csrfToken,
                cookie = headers.cookie
            )
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "Error getting safety alert: ${e.message}", e)
            null
        }
    }

    override suspend fun getSafetyAlertList(): List<SafetyAlertDto> {
        return         try {
            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("SafetyAlertRepo", "=== SAFETY ALERT REPOSITORY DEBUG ===")
            android.util.Log.d("SafetyAlertRepo", "Loading safety alerts for business: '$businessId', site: '$siteId'")
            
            // Create filter for business and site context
            val businessFilter = if (siteId != null && siteId.isNotEmpty()) {
                "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "BusinessId == Guid.Parse(\"$businessId\")"
            }
            android.util.Log.d("SafetyAlertRepo", "Safety alert filter: $businessFilter")
            
            val headers = headerManager.getHeaders().getOrThrow()
            val response = api.getSafetyAlertList(
                csrfToken = headers.csrfToken,
                cookie = headers.cookie,
                filter = businessFilter,
                businessId = businessId
            )
            
            val result = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
            android.util.Log.d("SafetyAlertRepo", "Safety alerts loaded: ${result.size} for business '$businessId'")
            android.util.Log.d("SafetyAlertRepo", "=====================================")
            result
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "Error getting safety alert list: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun saveSafetyAlert(alert: SafetyAlertDto): SafetyAlertDto? {
        return         try {
            // ✅ PASO 1: Obtener businessId y siteId desde BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            // ✅ PASO 2: Logs de debugging obligatorios
            android.util.Log.d("SafetyAlertRepo", "=== SAFETY ALERT REPOSITORY DEBUG ===")
            android.util.Log.d("SafetyAlertRepo", "Original DTO businessId: '${alert.businessId}', siteId: '${alert.siteId}'")
            android.util.Log.d("SafetyAlertRepo", "BusinessId from BusinessContextManager: '$businessId'")
            android.util.Log.d("SafetyAlertRepo", "SiteId from BusinessContextManager: '$siteId'")
            
            // ✅ PASO 3: Asignar businessId y siteId al DTO
            val alertWithContext = alert.copy(
                businessId = businessId,
                siteId = siteId
            )
            android.util.Log.d("SafetyAlertRepo", "Updated DTO businessId: '${alertWithContext.businessId}', siteId: '${alertWithContext.siteId}'")
            
            // ✅ PASO 4: Serializar JSON con businessId y siteId
            val entityJson = gson.toJson(alertWithContext)
            android.util.Log.d("SafetyAlertRepo", "JSON enviado a API: $entityJson")
            
            val headers = headerManager.getHeaders().getOrThrow()
            
            // ✅ PASO 5: Pasar businessId al API call
            android.util.Log.d("SafetyAlertRepo", "Calling API with businessId: '$businessId'")
            val response = api.saveSafetyAlert(
                entity = entityJson,
                csrfToken = headers.csrfToken,
                cookie = headers.cookie,
                businessId = businessId
            )
            
            if (!response.isSuccessful) {
                android.util.Log.e("SafetyAlertRepo", "Error saving safety alert. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
            } else {
                android.util.Log.d("SafetyAlertRepo", "API response received successfully")
            }
            android.util.Log.d("SafetyAlertRepo", "=====================================")
            
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "Error saving safety alert: ${e.message}", e)
            null
        }
    }

    override suspend fun deleteSafetyAlert(alert: SafetyAlertDto): Boolean {
        return try {
            val headers = headerManager.getHeaders().getOrThrow()
            val response = api.deleteSafetyAlert(
                alert = alert,
                csrfToken = headers.csrfToken,
                cookie = headers.cookie
            )
            if (!response.isSuccessful) {
                android.util.Log.e("SafetyAlertRepo", "Error deleting safety alert. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "Error deleting safety alert: ${e.message}", e)
            false
        }
    }

    override suspend fun getSafetyAlertCount(): Int {
        return         try {
            // Get business and site context from BusinessContextManager
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            android.util.Log.d("SafetyAlertRepo", "Getting safety alert count for business: '$businessId', site: '$siteId'")
            
            // Create filter for business and site context
            val businessFilter = if (siteId != null && siteId.isNotEmpty()) {
                "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "BusinessId == Guid.Parse(\"$businessId\")"
            }
            
            val headers = headerManager.getHeaders().getOrThrow()
            val response = api.getDatasetSafetyAlertCount(
                csrfToken = headers.csrfToken,
                filter = businessFilter
            )
            
            val count = if (response.isSuccessful) response.body() ?: 0 else 0
            android.util.Log.d("SafetyAlertRepo", "Safety alert count for business '$businessId': $count")
            count
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "Error getting safety alert count: ${e.message}", e)
            0
        }
    }
} 