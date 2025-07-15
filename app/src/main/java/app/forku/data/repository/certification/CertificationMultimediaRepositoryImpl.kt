package app.forku.data.repository.certification

import app.forku.data.api.CertificationMultimediaApi
import app.forku.data.api.dto.certification.CertificationMultimediaDto
import app.forku.domain.repository.certification.CertificationMultimediaRepository
import app.forku.core.business.BusinessContextManager
import javax.inject.Inject
import android.util.Log

class CertificationMultimediaRepositoryImpl @Inject constructor(
    private val api: CertificationMultimediaApi,
    private val businessContextManager: BusinessContextManager
) : CertificationMultimediaRepository {

    companion object {
        private const val TAG = "CertificationMultimediaRepo"
    }

    override suspend fun addCertificationMultimedia(entityJson: String): Result<CertificationMultimediaDto> = runCatching {
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] ========== SAVING MULTIMEDIA ==========")
        
        val businessId = businessContextManager.getCurrentBusinessId()
        val siteId = businessContextManager.getCurrentSiteId()
        
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] BusinessId: $businessId (for audit/tracking)")
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] SiteId: $siteId (for audit/tracking)")
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] Entity JSON: $entityJson")
        
        val response = api.saveCertificationMultimedia(entityJson, businessId, siteId)
        
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] Response code: ${response.code()}")
        
        if (response.isSuccessful) {
            val result = response.body() ?: throw Exception("Empty response body")
            Log.d(TAG, "📸 [CERT_MULTIMEDIA] ✅ SUCCESS: Multimedia saved with ID: ${result.id}")
            Log.d(TAG, "📸 [CERT_MULTIMEDIA] ========== COMPLETED ==========")
            result
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, "📸 [CERT_MULTIMEDIA] ❌ API ERROR: ${response.code()} - $errorBody")
            throw Exception("Failed to add certification multimedia: ${response.code()} - $errorBody")
        }
    }

    override suspend fun getCertificationMultimediaById(id: String): Result<CertificationMultimediaDto> = runCatching {
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] Getting multimedia by ID: $id")
        
        val response = api.getCertificationMultimediaById(id)
        
        if (response.isSuccessful) {
            val result = response.body() ?: throw Exception("Multimedia not found")
            Log.d(TAG, "📸 [CERT_MULTIMEDIA] ✅ Found multimedia: ${result.id}")
            result
        } else {
            Log.e(TAG, "📸 [CERT_MULTIMEDIA] ❌ Failed to get multimedia: ${response.code()}")
            throw Exception("Failed to get certification multimedia: ${response.code()}")
        }
    }

    override suspend fun getCertificationMultimediaByCertificationId(certificationId: String): Result<List<CertificationMultimediaDto>> = runCatching {
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] Getting multimedia for certification: $certificationId")
        
        // Use OData filter to get multimedia by CertificationId (following IncidentMultimedia pattern)
        val filter = "CertificationId == Guid.Parse(\"$certificationId\")"
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] Using OData filter: $filter")
        // Note: ImageUrl calculated field is not supported by backend, so we'll construct URLs manually
        
        val response = api.getCertificationMultimediaByCertificationId(filter)
        
        if (response.isSuccessful) {
            val result = response.body() ?: emptyList()
            Log.d(TAG, "📸 [CERT_MULTIMEDIA] ✅ Found ${result.size} multimedia items for certification $certificationId")
            result
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, "📸 [CERT_MULTIMEDIA] ❌ Failed to get multimedia for certification: ${response.code()} - $errorBody")
            
            // If filter fails, try without filter and client-side filtering as fallback
            Log.d(TAG, "📸 [CERT_MULTIMEDIA] Trying fallback without filter...")
            val fallbackResponse = api.getAllCertificationMultimedia()
            
            if (fallbackResponse.isSuccessful) {
                val allMultimedia = fallbackResponse.body() ?: emptyList()
                val filtered = allMultimedia.filter { it.certificationId == certificationId }
                Log.d(TAG, "📸 [CERT_MULTIMEDIA] ✅ Fallback found ${filtered.size} multimedia items for certification $certificationId")
                filtered
            } else {
                throw Exception("Failed to get certification multimedia: ${response.code()} - $errorBody")
            }
        }
    }

    override suspend fun deleteCertificationMultimedia(id: String): Result<Unit> = runCatching {
        Log.d(TAG, "📸 [CERT_MULTIMEDIA] Deleting multimedia: $id")
        
        val response = api.deleteCertificationMultimedia(id)
        
        if (response.isSuccessful) {
            Log.d(TAG, "📸 [CERT_MULTIMEDIA] ✅ Multimedia deleted: $id")
        } else {
            Log.e(TAG, "📸 [CERT_MULTIMEDIA] ❌ Failed to delete multimedia: ${response.code()}")
            throw Exception("Failed to delete certification multimedia: ${response.code()}")
        }
    }
} 