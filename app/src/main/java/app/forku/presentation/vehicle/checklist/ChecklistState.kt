package app.forku.presentation.vehicle.checklist

import app.forku.domain.model.Vehicle
import app.forku.domain.model.Checklist
import app.forku.domain.model.ChecklistItem

data class ChecklistState(
    val vehicle: Vehicle? = null,
    val checkItems: List<ChecklistItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
) 