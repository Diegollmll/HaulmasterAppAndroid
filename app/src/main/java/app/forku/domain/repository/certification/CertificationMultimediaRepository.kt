package app.forku.domain.repository.certification

import app.forku.data.api.dto.certification.CertificationMultimediaDto

interface CertificationMultimediaRepository {
    suspend fun addCertificationMultimedia(entityJson: String): Result<CertificationMultimediaDto>
    suspend fun getCertificationMultimediaById(id: String): Result<CertificationMultimediaDto>
    suspend fun getCertificationMultimediaByCertificationId(certificationId: String): Result<List<CertificationMultimediaDto>>
    suspend fun deleteCertificationMultimedia(id: String): Result<Unit>
} 