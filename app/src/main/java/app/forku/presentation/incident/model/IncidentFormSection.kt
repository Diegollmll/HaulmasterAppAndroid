package app.forku.presentation.incident.model

sealed class IncidentFormSection {
    object BasicInfo : IncidentFormSection()
    object PeopleInvolved : IncidentFormSection()
    object VehicleInfo : IncidentFormSection()
    object IncidentDetails : IncidentFormSection()
    object RootCauseAnalysis : IncidentFormSection()
    object Documentation : IncidentFormSection()
} 