package app.forku.domain.repository.certification

import app.forku.domain.model.certification.Certification

interface CertificationRepository {
    suspend fun getCertifications(userId: String? = null): List<Certification>
    suspend fun getCertificationById(id: String): Certification?
    suspend fun createCertification(certification: Certification, userId: String): Result<Certification>
    suspend fun updateCertification(certification: Certification): Result<Certification>
    suspend fun deleteCertification(id: String): Result<Boolean>
} 