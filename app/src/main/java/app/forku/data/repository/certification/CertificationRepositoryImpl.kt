package app.forku.data.repository.certification

import app.forku.data.api.CertificationApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import javax.inject.Inject
import com.google.gson.Gson

class CertificationRepositoryImpl @Inject constructor(
    private val api: CertificationApi,
    private val authDataStore: AuthDataStore
) : CertificationRepository {

    override suspend fun getCertifications(userId: String?): List<Certification> {
        return try {
            val response = api.getCertifications(userId)
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
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val entity = Gson().toJson(certification.toDto())
            val response = api.createUpdateCertification(csrfToken, cookie, entity)
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
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val entity = Gson().toJson(certification.toDto())
            val response = api.createUpdateCertification(csrfToken, cookie, entity)
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
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val response = api.deleteCertification(id, csrfToken, cookie)
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

    suspend fun getCertificationsByGoUserId2(userGuid: String): List<Certification> {
        return try {
            val csrfToken = authDataStore.getCsrfTokenSuspend() ?: ""
            val cookie = authDataStore.getAntiforgeryCookieSuspend() ?: ""
            val filter = "GOUserId2 == Guid.Parse(\"$userGuid\")"
            val response = api.getCertificationsByUserId(filter, csrfToken, cookie)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.map { it.toDomain() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("Certification", "Error getting certifications by GOUserId2", e)
            emptyList()
        }
    }
} 