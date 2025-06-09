package app.forku.domain.model.vehicle

import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.checklist.ChecklistAnswer

/**
 * Vehicle with related data optimized for list views
 */
data class VehicleWithRelatedData(
    val vehicle: Vehicle,
    val activeSessions: List<VehicleSessionInfo> = emptyList(),
    val lastPreShiftCheck: ChecklistAnswer? = null,
    val checklistAnswers: List<ChecklistAnswer> = emptyList()
) 