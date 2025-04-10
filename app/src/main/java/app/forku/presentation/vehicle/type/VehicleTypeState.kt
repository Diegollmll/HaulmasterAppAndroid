package app.forku.presentation.vehicle.type

import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.model.vehicle.VehicleType

data class VehicleTypeState(
    val types: List<VehicleType> = emptyList(),
    val categories: List<VehicleCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedType: VehicleType? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)