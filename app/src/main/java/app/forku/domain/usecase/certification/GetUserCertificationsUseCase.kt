package app.forku.domain.usecase.certification

import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import javax.inject.Inject

class GetUserCertificationsUseCase @Inject constructor(
    private val repository: CertificationRepository
) {
    suspend operator fun invoke(userId: String? = null): List<Certification> {
        android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE] ========== STARTING ==========")
        android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE] Input userId: $userId")
        android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE] Repository type: ${repository::class.java.simpleName}")
        
        // 🔄 SIMPLIFIED APPROACH: Always use the interface method
        // The repository implementation will decide the best strategy internally
        android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE] Using unified approach - calling repository.getCertifications()")
        
        return repository.getCertifications(userId).also { result ->
            android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE] ========== COMPLETED ==========")
            android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE] Final result: ${result.size} certifications")
            
            // Log each certification for debugging
            result.forEachIndexed { index, cert ->
                android.util.Log.d("GetUserCertUseCase", "🎯 [USE_CASE]   [$index] ${cert.name} (${cert.id}) - User: ${cert.userId}")
            }
        }
    }
}

class GetCertificationByIdUseCase @Inject constructor(
    private val repository: CertificationRepository
) {
    suspend operator fun invoke(id: String): Certification? =
        repository.getCertificationById(id)
}

class UpdateCertificationUseCase @Inject constructor(
    private val repository: CertificationRepository
) {
    suspend operator fun invoke(certification: Certification): Result<Certification> =
        repository.updateCertification(certification)
}

class DeleteCertificationUseCase @Inject constructor(
    private val repository: CertificationRepository
) {
    suspend operator fun invoke(id: String): Result<Boolean> =
        repository.deleteCertification(id)
} 