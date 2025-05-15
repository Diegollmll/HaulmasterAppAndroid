package app.forku.domain.model.vehicle

import androidx.compose.ui.graphics.Color

fun VehicleStatus.toColor(): Color {
    return when(this) {
        VehicleStatus.AVAILABLE -> Color(0xFF448AFF) // Blue
        VehicleStatus.IN_USE -> Color(0xFF00C853) // Green
        VehicleStatus.OUT_OF_SERVICE -> Color(0xFFE53935) // Red
        VehicleStatus.MAINTENANCE -> TODO()
    }
}

fun VehicleStatus.toDisplayString(): String = when (this) {
    VehicleStatus.AVAILABLE -> "Available"
    VehicleStatus.IN_USE -> "In Use"
    VehicleStatus.OUT_OF_SERVICE -> "Out of Service"
    VehicleStatus.MAINTENANCE -> "Maintenance"
}

fun VehicleStatus.getDescription(): String = when (this) {
    VehicleStatus.IN_USE -> "The vehicle is currently being used"
    VehicleStatus.OUT_OF_SERVICE -> "The vehicle is not available for use"
    VehicleStatus.AVAILABLE -> "The vehicle is ready to be used"
    VehicleStatus.MAINTENANCE -> TODO()
}

fun VehicleStatus.getErrorMessage(): String {
    return when (this) {
        VehicleStatus.IN_USE -> "Vehicle is already in use"
        VehicleStatus.OUT_OF_SERVICE -> "Vehicle is currently out of service"
        VehicleStatus.AVAILABLE -> "" // No error message for available status
        VehicleStatus.MAINTENANCE -> TODO()
    }
}

fun VehicleStatus.isAvailable(): Boolean {
    return this == VehicleStatus.AVAILABLE
}

