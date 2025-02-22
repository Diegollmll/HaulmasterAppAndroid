package app.forku.data.mapper

import app.forku.data.api.dto.VehicleDto
import app.forku.data.api.dto.VehicleTypeDto
import app.forku.data.api.dto.VehicleCheckDto
import app.forku.domain.model.Vehicle
import app.forku.domain.model.VehicleType
import app.forku.domain.model.VehicleCheck


fun VehicleDto.toDomain(): Vehicle {
    return Vehicle(
        id = id,
        type = type.toDomain(),
        status = VehicleStatus.fromString(status),
        serialNumber = serialNumber,
        qrCode = qrCode,
        lastCheck = lastCheck.toDomain()
    )
}

fun VehicleTypeDto.toDomain(): VehicleType {
    return VehicleType(
        id = id,
        name = name,
        requiresCertification = requiresCertification
    )
}

fun VehicleCheckDto.toDomain(): VehicleCheck {
    return VehicleCheck(
        timestamp = timestamp,
        status = status
    )
} 