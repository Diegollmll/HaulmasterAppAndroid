package app.forku.domain.usecase.certification

import app.forku.data.api.dto.certification.CertificationMultimediaDto
import app.forku.domain.repository.certification.CertificationMultimediaRepository
import javax.inject.Inject

class AddCertificationMultimediaUseCase @Inject constructor(
    private val repository: CertificationMultimediaRepository
) {
    suspend operator fun invoke(entityJson: String): Result<CertificationMultimediaDto> {
        return repository.addCertificationMultimedia(entityJson)
    }
} 