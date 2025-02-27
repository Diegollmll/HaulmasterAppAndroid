package app.forku.presentation.incident.list

import app.forku.domain.model.incident.Incident

data class IncidentListState(
    val incidents: List<Incident> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 