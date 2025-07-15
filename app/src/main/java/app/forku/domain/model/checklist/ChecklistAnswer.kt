package app.forku.domain.model.checklist

data class ChecklistAnswer(
    val id: String = "",
    val checklistId: String = "",
    val checklistVersion: String = "1.0",
    val goUserId: String = "",
    val startDateTime: String = "",
    val endDateTime: String = "",
    val status: Int = 0,
    val locationCoordinates: String? = null,
    val isDirty: Boolean = true,
    val isNew: Boolean = true,
    val isMarkedForDeletion: Boolean = false,
    val lastCheckDateTime: String = "",
    val vehicleId: String = "",
    val duration: Int? = null,
    val businessId: String? = null,
    val operatorName: String = "Unknown",
    val vehicleName: String = "Unknown"
) 