package app.forku.presentation.dashboard

import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.model.checklist.PreShiftCheck


data class AdminDashboardState(
    val operatingVehiclesCount: Int = 0,
    val totalIncidentsCount: Int = 0,
    val safetyAlertsCount: Int = 0,
    val activeVehicleSessions: List<VehicleSessionInfo> = emptyList(),
    val activeOperators: List<OperatorSessionInfo> = emptyList(),
    val lastPreShiftChecks: Map<String, PreShiftCheck?> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val feedbackSubmitted: Boolean = false
)