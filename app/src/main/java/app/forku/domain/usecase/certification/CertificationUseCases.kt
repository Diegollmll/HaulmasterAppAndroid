package app.forku.domain.usecase.certification

import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import javax.inject.Inject

class GetUserCertificationsUseCase @Inject constructor(
    private val repository: CertificationRepository
) {
    suspend operator fun invoke(userId: String? = null): List<Certification> =
        if (userId != null) {
            // Use the new method for GOUserId2
            if (repository is app.forku.data.repository.certification.CertificationRepositoryImpl) {
                repository.getCertificationsByGoUserId2(userId)
            } else {
                repository.getCertifications(userId)
            }
        } else {
            repository.getCertifications(null)
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