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
        status = CertificationStatus.entries.getOrNull(status) ?: CertificationStatus.PENDING,
        userId = goUserId ?: "",
        issuer = issuer,
        certificationCode = certificationCode,
        documentUrl = null,
        timestamp = timestamp,
        isMarkedForDeletion = isMarkedForDeletion,
        isDirty = isDirty,
        isNew = isNew,
        internalObjectId = internalObjectId,
        businessId = businessId,
        siteId = siteId // ✅ Include siteId from DTO
    )
}

fun Certification.toDto(): CertificationDto {
    return CertificationDto(
        id = id,
        name = name,
        description = description,
        issuedDate = issuedDate,
        expiryDate = expiryDate,
        status = status.ordinal,
        goUserId = userId,
        issuer = issuer,
        certificationCode = certificationCode,
        timestamp = timestamp,
        isMarkedForDeletion = isMarkedForDeletion,
        isDirty = isDirty,
        isNew = isNew,
        internalObjectId = internalObjectId,
        businessId = businessId,
        siteId = siteId // ✅ Include siteId from domain
    )
}
