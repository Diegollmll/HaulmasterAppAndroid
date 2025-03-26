package app.forku.data.repository.certification

import app.forku.data.api.CertificationApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toDto
import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import javax.inject.Inject

class CertificationRepositoryImpl @Inject constructor(
    private val api: CertificationApi
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
            val response = api.createCertification(certification.toDto(), userId)
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
            val response = api.updateCertification(certification.id, certification.toDto())
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
            val response = api.deleteCertification(id)
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
} 