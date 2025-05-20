package app.forku.data.mapper

import app.forku.data.api.dto.checklist.ChecklistAnswerDto
import app.forku.domain.model.checklist.ChecklistAnswer
import com.google.gson.JsonObject

fun ChecklistAnswer.toDto() = ChecklistAnswerDto(
    checklistId = checklistId,
    endDateTime = endDateTime,
    goUserId = goUserId,
    id = id,
    startDateTime = startDateTime,
    status = status,
    locationCoordinates = locationCoordinates,
    isDirty = isDirty,
    isNew = isNew,
    isMarkedForDeletion = isMarkedForDeletion,
    lastCheckDateTime = lastCheckDateTime,
    vehicleId = vehicleId,
    duration = duration
)

fun ChecklistAnswerDto.toDomain() = ChecklistAnswer(
    id = id ?: "",
    checklistId = checklistId,
    goUserId = goUserId,
    startDateTime = startDateTime,
    endDateTime = endDateTime ?: "",
    status = status,
    locationCoordinates = locationCoordinates,
    isDirty = isDirty,
    isNew = isNew,
    isMarkedForDeletion = isMarkedForDeletion,
    lastCheckDateTime = lastCheckDateTime,
    vehicleId = vehicleId,
    duration = duration
)

fun ChecklistAnswerDto.toJsonObject(): JsonObject {
    android.util.Log.d("ChecklistAnswerMapper", "[toJsonObject] checklistId: $checklistId, endDateTime: $endDateTime, goUserId: $goUserId, id: $id, startDateTime: $startDateTime, status: $status, isDirty: $isDirty, isNew: $isNew, isMarkedForDeletion: $isMarkedForDeletion, locationCoordinates: $locationCoordinates, lastCheckDateTime: $lastCheckDateTime, vehicleId: $vehicleId")
    return JsonObject().apply {
        // Add type information if needed by backend
        // addProperty("$type", "ChecklistAnswerDataObject")
        addProperty("ChecklistId", checklistId)
        addProperty("EndDateTime", endDateTime)
        addProperty("GOUserId", goUserId)
        addProperty("Id", id ?: "")
        addProperty("StartDateTime", startDateTime)
        addProperty("Status", status)
        addProperty("LocationCoordinates", locationCoordinates)
        addProperty("IsDirty", isDirty)
        addProperty("IsNew", isNew)
        addProperty("IsMarkedForDeletion", isMarkedForDeletion)
        addProperty("LastCheckDateTime", lastCheckDateTime)
        addProperty("VehicleId", vehicleId)
        duration?.let { addProperty("Duration", it) }
    }
} 