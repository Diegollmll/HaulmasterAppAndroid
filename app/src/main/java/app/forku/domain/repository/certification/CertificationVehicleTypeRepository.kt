package app.forku.domain.repository.certification

import app.forku.domain.model.certification.CertificationVehicleType

interface CertificationVehicleTypeRepository {
    suspend fun getCertificationVehicleTypes(certificationId: String? = null): List<CertificationVehicleType>
    suspend fun getCertificationVehicleTypeById(id: String): CertificationVehicleType?
    suspend fun getCertificationVehicleTypesByCertificationId(certificationId: String): List<CertificationVehicleType>
    suspend fun createCertificationVehicleType(certificationVehicleType: CertificationVehicleType): Result<CertificationVehicleType>
    suspend fun updateCertificationVehicleType(certificationVehicleType: CertificationVehicleType): Result<CertificationVehicleType>
    suspend fun deleteCertificationVehicleType(id: String): Result<Boolean>
    suspend fun deleteCertificationVehicleTypesByCertificationId(certificationId: String): Result<Boolean>
} 