package app.forku.data.mapper

import app.forku.data.api.dto.certification.CertificationVehicleTypeDto
import app.forku.domain.model.certification.CertificationVehicleType

fun CertificationVehicleTypeDto.toDomain(): CertificationVehicleType {
    return CertificationVehicleType(
        id = id,
        certificationId = certificationId,
        vehicleTypeId = vehicleTypeId,
        siteId = siteId,
        timestamp = timestamp ?: java.time.Instant.now().toString(),
        isMarkedForDeletion = isMarkedForDeletion,
        isDirty = isDirty,
        isNew = isNew,
        internalObjectId = internalObjectId
    )
}

fun CertificationVehicleType.toDto(): CertificationVehicleTypeDto {
    return CertificationVehicleTypeDto(
        id = id,
        certificationId = certificationId,
        vehicleTypeId = vehicleTypeId,
        siteId = siteId,
        timestamp = timestamp,
        isMarkedForDeletion = isMarkedForDeletion,
        isDirty = isDirty,
        isNew = isNew,
        internalObjectId = internalObjectId
    )
} 