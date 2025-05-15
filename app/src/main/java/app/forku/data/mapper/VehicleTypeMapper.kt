package app.forku.data.mapper

import app.forku.data.api.dto.vehicle.VehicleTypeDto
import app.forku.domain.model.vehicle.VehicleType

fun VehicleTypeDto.toDomain(): VehicleType = VehicleType(
    Id = Id,
    Name = Name,
    RequiresCertification = RequiresCertification,
    VehicleCategoryId = VehicleCategoryId ?: "",
    IsMarkedForDeletion = IsMarkedForDeletion,
    InternalObjectId = InternalObjectId
)

fun VehicleType.toDto(): VehicleTypeDto = VehicleTypeDto(
    Id = Id,
    Name = Name,
    RequiresCertification = RequiresCertification,
    VehicleCategoryId = VehicleCategoryId,
    IsMarkedForDeletion = IsMarkedForDeletion,
    InternalObjectId = InternalObjectId
)


