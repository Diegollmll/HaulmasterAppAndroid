package app.forku.domain.model.vehicle

import androidx.compose.ui.graphics.Color

fun VehicleStatus.toColor(): Color {
    return when(this) {
        VehicleStatus.AVAILABLE -> Color(0xFF448AFF) // Blue
        VehicleStatus.IN_USE -> Color(0xFF00C853) // Green
        VehicleStatus.BLOCKED -> Color(0xFFE53935) // Red
        VehicleStatus.UNKNOWN -> Color(0xFF9E9E9E) // Gray
    }
}

fun VehicleStatus.toDisplayString(): String = when (this) {
    VehicleStatus.IN_USE -> "In Use"
    VehicleStatus.BLOCKED -> "Blocked"
    VehicleStatus.AVAILABLE -> "Available"
    VehicleStatus.UNKNOWN -> "Unknown"
}