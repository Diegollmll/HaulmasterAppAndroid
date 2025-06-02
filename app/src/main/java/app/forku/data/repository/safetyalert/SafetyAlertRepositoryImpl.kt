package app.forku.data.repository.safetyalert

import app.forku.data.api.SafetyAlertApi
import app.forku.data.api.dto.safetyalert.SafetyAlertDto
import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import app.forku.core.auth.HeaderManager
import com.google.gson.Gson
import javax.inject.Inject

class SafetyAlertRepositoryImpl @Inject constructor(
    private val api: SafetyAlertApi,
    private val headerManager: HeaderManager,
    private val gson: Gson
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
        return try {
            val headers = headerManager.getHeaders().getOrThrow()
            val response = api.getSafetyAlertList(
                csrfToken = headers.csrfToken,
                cookie = headers.cookie
            )
            if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "Error getting safety alert list: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun saveSafetyAlert(alert: SafetyAlertDto): SafetyAlertDto? {
        return try {
            val headers = headerManager.getHeaders().getOrThrow()
            // Convert the DTO to a JSON string and wrap it in an entity field
            val entityJson = gson.toJson(alert)
            android.util.Log.d("SafetyAlertRepo", "Saving safety alert with entity: $entityJson")
            val response = api.saveSafetyAlert(
                entity = entityJson,
                csrfToken = headers.csrfToken,
                cookie = headers.cookie
            )
            if (!response.isSuccessful) {
                android.util.Log.e("SafetyAlertRepo", "Error saving safety alert. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
            }
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
        return try {
            val headers = headerManager.getHeaders().getOrThrow()
            val response = api.getDatasetSafetyAlertCount(
                csrfToken = headers.csrfToken,
                accept = "text/plain"
            )
            if (!response.isSuccessful) {
                android.util.Log.e("SafetyAlertRepo", "getSafetyAlertCount: Response not successful. Code: ${response.code()}, ErrorBody: ${response.errorBody()?.string()}")
            } else {
                android.util.Log.d("SafetyAlertRepo", "getSafetyAlertCount: Success. Code: ${response.code()}, Body: ${response.body()}")
            }
            if (response.isSuccessful) response.body() ?: 0 else 0
        } catch (e: Exception) {
            android.util.Log.e("SafetyAlertRepo", "getSafetyAlertCount: Exception: ${e.message}", e)
            0
        }
    }
} 