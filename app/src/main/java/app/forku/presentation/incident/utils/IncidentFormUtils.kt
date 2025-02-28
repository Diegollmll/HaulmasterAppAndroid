import app.forku.domain.model.incident.IncidentType

fun getActivitiesByType(type: IncidentType?): List<String> = when (type) {
    IncidentType.COLLISION -> listOf(
        "Loading",
        "Unloading",
        "Traveling",
        "Maneuvering",
        "Parking",
        "Other"
    )
    IncidentType.NEAR_MISS -> listOf(
        "Pedestrian Interaction",
        "Vehicle Operation",
        "Load Handling",
        "Other"
    )
    IncidentType.HAZARD -> listOf(
        "Regular Operation",
        "Inspection",
        "Maintenance",
        "Other"
    )
    IncidentType.VEHICLE_FAIL -> listOf(
        "Regular Operation",
        "Start-up",
        "Maintenance Check",
        "Other"
    )
    else -> emptyList()
}

fun getImmediateActionsByType(type: IncidentType?): List<String> = when (type) {
    IncidentType.COLLISION -> listOf(
        "Secured the area",
        "Provided first aid",
        "Called emergency services",
        "Reported to supervisor",
        "Other"
    )
    // Add other type-specific actions...
    else -> emptyList()
}

fun getProposedSolutionsByType(type: IncidentType?): List<String> = when (type) {
    IncidentType.COLLISION -> listOf(
        "Training refresher",
        "Equipment upgrades",
        "Procedural changes",
        "Environmental modifications",
        "Other"
    )
    // Add other type-specific solutions...
    else -> emptyList()
} 