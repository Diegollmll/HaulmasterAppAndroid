package app.forku.presentation.checklist

/**
 * UI State representation for a Pre-Shift Check
 * Used across multiple screens and ViewModels for consistency
 */
data class PreShiftCheckState(
    val id: String,
    val vehicleId: String,
    val vehicleCodename: String,
    val operatorName: String,
    val status: String,
    val lastCheckDateTime: String? = null
) 