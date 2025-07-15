package app.forku.presentation.checklist

import app.forku.domain.model.checklist.Checklist
import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.model.checklist.AnsweredChecklistItem
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.model.Site
import app.forku.data.api.dto.checklist.ChecklistItemAnswerMultimediaDto

data class CheckDetailState(
    val isLoading: Boolean = false,
    val check: PreShiftCheckState? = null,
    val checklist: Checklist? = null,
    val checklistAnswer: ChecklistAnswer? = null,
    val answeredItems: List<AnsweredChecklistItem> = emptyList(),
    val vehicle: Vehicle? = null,
    val vehicleType: VehicleType? = null,
    val vehicleCategory: VehicleCategory? = null,
    val site: Site? = null,
    val multimediaByAnswerId: Map<String, List<ChecklistItemAnswerMultimediaDto>> = emptyMap(),
    val error: String? = null
) 