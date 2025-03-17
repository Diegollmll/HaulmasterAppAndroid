package app.forku.presentation.safety

data class SafetyAlertsState(
    val safetyAlerts: List<SafetyAlert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SafetyAlert(
    val id: String,
    val vehicleId: String,
    val vehicleCodename: String,
    val description: String,
    val operatorId: String,
    val operatorName: String,
    val date: String
) 