package app.forku.presentation.vehicle.category

import app.forku.domain.model.vehicle.VehicleCategory

data class VehicleCategoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<VehicleCategory> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedCategory: VehicleCategory? = null
) 