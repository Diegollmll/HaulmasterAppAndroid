package app.forku.presentation.incident

import app.forku.domain.model.incident.IncidentType

data class IncidentReportState(
    val type: IncidentType? = null,
    val description: String = "",
    val vehicleId: String? = null,
    val sessionId: String? = null,
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false
) 