package app.forku.presentation.scanner

import app.forku.domain.model.vehicle.Vehicle

data class QRScannerState(
    val vehicle: Vehicle? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val canStartCheck: Boolean = false,
    val navigateToProfile: Boolean = false,
    val navigateToChecklist: Boolean = false
)