import app.forku.domain.model.incident.IncidentTypeEnum

fun getActivitiesByType(type: IncidentTypeEnum?): List<String> = when (type) {
    IncidentTypeEnum.COLLISION -> listOf(
        "Loading",
        "Unloading",
        "Traveling",
        "Maneuvering",
        "Parking",
        "Other"
    )
    IncidentTypeEnum.NEAR_MISS -> listOf(
        "Pedestrian Interaction",
        "Vehicle Operation",
        "Load Handling",
        "Other"
    )
    IncidentTypeEnum.HAZARD -> listOf(
        "Regular Operation",
        "Inspection",
        "Maintenance",
        "Other"
    )
    IncidentTypeEnum.VEHICLE_FAIL -> listOf(
        "Regular Operation",
        "Start-up",
        "Maintenance Check",
        "Other"
    )
    else -> emptyList()
}

fun getImmediateActionsByType(type: IncidentTypeEnum?): List<String> = when (type) {
    IncidentTypeEnum.COLLISION -> listOf(
        "Secured the area",
        "Provided first aid",
        "Called emergency services",
        "Reported to supervisor",
        "Other"
    )
    // Add other type-specific actions...
    else -> emptyList()
}

fun getProposedSolutionsByType(type: IncidentTypeEnum?): List<String> = when (type) {
    IncidentTypeEnum.COLLISION -> listOf(
        "Training refresher",
        "Equipment upgrades",
        "Procedural changes",
        "Environmental modifications",
        "Other"
    )
    // Add other type-specific solutions...
    else -> emptyList()
} 