package app.forku.data.mapper

import app.forku.data.api.dto.certification.CertificationDto
import app.forku.domain.model.certification.Certification
import app.forku.domain.model.certification.CertificationStatus

fun CertificationDto.toDomain(): Certification {
    return Certification(
        id = id,
        name = name,
        description = description,
        issuedDate = issuedDate,
        expiryDate = expiryDate,
        status = CertificationStatus.fromString(status),
        userId = userId,
        issuer = issuer,
        certificationCode = certificationCode,
        documentUrl = documentUrl,
        timestamp = timestamp
    )
}

fun Certification.toDto(): CertificationDto {
    return CertificationDto(
        id = id,
        name = name,
        description = description,
        issuedDate = issuedDate,
        expiryDate = expiryDate,
        status = status.name,
        userId = userId,
        issuer = issuer,
        certificationCode = certificationCode,
        documentUrl = documentUrl,
        timestamp = timestamp
    )
} 