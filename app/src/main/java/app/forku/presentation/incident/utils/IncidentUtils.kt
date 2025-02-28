package app.forku.presentation.incident.utils

import app.forku.domain.model.incident.IncidentType

fun getProposedSolutionsByType(type: IncidentType?): List<String> {
    return when (type) {
        IncidentType.COLLISION -> listOf(
            "Review and update safety procedures",
            "Additional operator training",
            "Improve visibility in area",
            "Install warning signals/signs"
        )
        IncidentType.NEAR_MISS -> listOf(
            "Review safety protocols",
            "Update risk assessment",
            "Additional safety equipment",
            "Improve communication procedures"
        )
        IncidentType.HAZARD -> listOf(
            "Remove or isolate hazard",
            "Install protective barriers",
            "Update warning signage",
            "Modify work procedures"
        )
        IncidentType.VEHICLE_FAIL -> listOf(
            "Review maintenance schedule",
            "Update inspection procedures",
            "Replace faulty components",
            "Operator training on vehicle checks"
        )
        null -> emptyList()
    }
} 