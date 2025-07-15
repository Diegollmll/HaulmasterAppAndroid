package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistQuestionVehicleTypeDto
import app.forku.domain.model.checklist.ChecklistQuestionVehicleType

fun ChecklistQuestionVehicleTypeDto.toDomain(): ChecklistQuestionVehicleType {
    return ChecklistQuestionVehicleType(
        id = id,
        checklistItemId = checklistItemId,
        vehicleTypeId = vehicleTypeId,
        isMarkedForDeletion = isMarkedForDeletion,
        internalObjectId = internalObjectId
    )
}

fun ChecklistQuestionVehicleType.toDto(): ChecklistQuestionVehicleTypeDto {
    return ChecklistQuestionVehicleTypeDto(
        id = id,
        checklistItemId = checklistItemId,
        vehicleTypeId = vehicleTypeId,
        isMarkedForDeletion = isMarkedForDeletion,
        internalObjectId = internalObjectId
    )
} 