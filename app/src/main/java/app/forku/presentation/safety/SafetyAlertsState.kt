package app.forku.presentation.safety

data class SafetyAlertsState(
    val alerts: List<SafetyAlert> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SafetyAlert(
    val id: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String
) 