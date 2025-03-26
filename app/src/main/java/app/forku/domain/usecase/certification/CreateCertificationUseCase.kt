package app.forku.domain.usecase.certification

import app.forku.domain.model.certification.Certification
import app.forku.domain.repository.certification.CertificationRepository
import javax.inject.Inject

class CreateCertificationUseCase @Inject constructor(
    private val repository: CertificationRepository
) {
    suspend operator fun invoke(certification: Certification, userId: String): Result<Certification> {
        return repository.createCertification(certification, userId)
    }
} 