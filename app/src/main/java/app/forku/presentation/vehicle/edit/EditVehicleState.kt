package app.forku.presentation.vehicle.edit

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.VehicleCategory
import app.forku.domain.model.vehicle.EnergySource
import app.forku.domain.model.user.UserRole
import app.forku.presentation.dashboard.Business

data class EditVehicleState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val loadSuccess: Boolean = false,

    // Vehicle Data
    val vehicleId: String? = null,
    val initialVehicle: Vehicle? = null, // Original data loaded

    // Dropdown Data
    val vehicleTypes: List<VehicleType> = emptyList(),
    val vehicleCategories: List<VehicleCategory> = emptyList(),
    val energySources: List<EnergySource> = listOf(EnergySource.ELECTRIC, EnergySource.LPG, EnergySource.DIESEL),
    val businesses: List<Business> = emptyList(), // For admin roles

    // Selected Values (managed by ViewModel based on initialVehicle and user edits)
    val selectedType: VehicleType? = null,
    val selectedCategory: VehicleCategory? = null,
    val selectedEnergySource: EnergySource? = null,
    var selectedBusinessId: String? = null, // For admin roles

    // User Info
    val currentUserRole: UserRole? = null
) 