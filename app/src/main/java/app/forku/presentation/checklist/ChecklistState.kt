package app.forku.presentation.checklist

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.checklist.ChecklistItem
import app.forku.domain.model.checklist.RotationRules

data class ChecklistState(
    val vehicle: Vehicle? = null,
    val checkItems: List<ChecklistItem> = emptyList(),
    val rotationRules: RotationRules? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val isSubmitted: Boolean = false,
    val vehicleBlocked: Boolean = false,
    val error: String? = null
)