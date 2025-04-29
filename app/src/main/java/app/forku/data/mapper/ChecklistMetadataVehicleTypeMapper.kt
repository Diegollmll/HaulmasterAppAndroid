package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistMetadataVehicleTypeDto
import app.forku.domain.model.checklist.ChecklistMetadataVehicleType

fun ChecklistMetadataVehicleTypeDto.toDomain(): ChecklistMetadataVehicleType =
    ChecklistMetadataVehicleType(
        id = id,
        vehicleTypeId = vehicleTypeId,
        metadata = metadata
    )

fun ChecklistMetadataVehicleType.toDto(): ChecklistMetadataVehicleTypeDto =
    ChecklistMetadataVehicleTypeDto(
        id = id,
        vehicleTypeId = vehicleTypeId,
        metadata = metadata
    ) 