package app.forku.data.repository.certification

import app.forku.data.api.CertificationApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import javax.inject.Inject
import com.google.gson.Gson
import app.forku.core.business.BusinessContextManager

class CertificationRepositoryImpl @Inject constructor(
    private val api: CertificationApi,
    private val authDataStore: AuthDataStore,
    private val businessContextManager: BusinessContextManager
) : CertificationRepository {

    override suspend fun getCertifications(userId: String?): List<Certification> {
        return try {
            // Get business and site context
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            // Apply filter for business and site context if userId is provided
            val filter = when {
                userId != null -> {
                    if (siteId != null && siteId.isNotEmpty()) {
                        "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
                    } else {
                        "GOUserId == Guid.Parse(\"$userId\") && BusinessId == Guid.Parse(\"$businessId\")"
                    }
                }
                else -> {
                    if (siteId != null && siteId.isNotEmpty()) {
                        "BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
                    } else {
                        "BusinessId == Guid.Parse(\"$businessId\")"
                    }
                }
            }
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            
            // Use filtered API call
            val response = api.getCertificationsByUserId(filter, csrfToken, cookie)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error getting certifications", e)
            emptyList()
        }
    }

    override suspend fun getCertificationById(id: String): Certification? {
        return try {
            val response = api.getCertificationById(id)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error getting certification by id", e)
            null
        }
    }

    override suspend fun createCertification(certification: Certification, userId: String): Result<Certification> {
        return try {
            // Get business and site context
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            // Add businessId and siteId to certification
            val certificationWithContext = certification.copy(
                businessId = businessId,
                siteId = siteId,
                userId = userId
            )
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val entity = Gson().toJson(certificationWithContext.toDto())
            
            // Add businessId to query params
            val response = api.createUpdateCertification(csrfToken, cookie, entity, businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to create certification: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error creating certification", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCertification(certification: Certification): Result<Certification> {
        return try {
            // Get business and site context
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            // Ensure businessId and siteId are set
            val certificationWithContext = certification.copy(
                businessId = certification.businessId ?: businessId,
                siteId = certification.siteId ?: siteId
            )
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val entity = Gson().toJson(certificationWithContext.toDto())
            
            // Add businessId to query params
            val response = api.createUpdateCertification(csrfToken, cookie, entity, businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Failed to update certification: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error updating certification", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCertification(id: String): Result<Boolean> {
        return try {
            // Get business context
            val businessId = businessContextManager.getCurrentBusinessId()
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val response = api.deleteCertification(id, csrfToken, cookie, businessId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete certification: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error deleting certification", e)
            Result.failure(e)
        }
    }

    suspend fun getCertificationsByGoUserId(userGuid: String): List<Certification> {
        return try {
            // Get business and site context
            val businessId = businessContextManager.getCurrentBusinessId()
            val siteId = businessContextManager.getCurrentSiteId()
            
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val filter = if (siteId != null && siteId.isNotEmpty()) {
                "GOUserId == Guid.Parse(\"$userGuid\") && BusinessId == Guid.Parse(\"$businessId\") && SiteId == Guid.Parse(\"$siteId\")"
            } else {
                "GOUserId == Guid.Parse(\"$userGuid\") && BusinessId == Guid.Parse(\"$businessId\")"
            }
            val response = api.getCertificationsByUserId(filter, csrfToken, cookie)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error getting certifications by GOUserId", e)
            emptyList()
        }
    }
} 