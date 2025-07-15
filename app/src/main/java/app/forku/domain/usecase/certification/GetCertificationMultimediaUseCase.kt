package app.forku.domain.usecase.certification

import app.forku.data.api.dto.certification.CertificationMultimediaDto
import app.forku.domain.repository.certification.CertificationMultimediaRepository
import javax.inject.Inject

class GetCertificationMultimediaUseCase @Inject constructor(
    private val repository: CertificationMultimediaRepository
) {
    suspend operator fun invoke(certificationId: String): Result<List<CertificationMultimediaDto>> {
        return repository.getCertificationMultimediaByCertificationId(certificationId)
    }
} 