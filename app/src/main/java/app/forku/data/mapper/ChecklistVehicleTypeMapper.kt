package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistVehicleTypeDto
import app.forku.domain.model.checklist.ChecklistVehicleType

fun ChecklistVehicleTypeDto.toDomain(): ChecklistVehicleType {
    return ChecklistVehicleType(
        id = Id,
        checklistId = ChecklistId,
        vehicleTypeId = VehicleTypeId,
        isMarkedForDeletion = IsMarkedForDeletion,
        internalObjectId = InternalObjectId
    )
}

fun ChecklistVehicleType.toDto(): ChecklistVehicleTypeDto {
    return ChecklistVehicleTypeDto(
        ChecklistId = checklistId,
        Id = id,
        VehicleTypeId = vehicleTypeId,
        IsMarkedForDeletion = isMarkedForDeletion,
        InternalObjectId = internalObjectId
    )
} 