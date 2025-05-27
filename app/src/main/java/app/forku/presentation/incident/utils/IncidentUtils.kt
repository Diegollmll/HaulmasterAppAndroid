package app.forku.presentation.incident.utils

import app.forku.domain.model.incident.IncidentTypeEnum



fun getProposedSolutionsByType(type: IncidentTypeEnum?): List<String> {
    return when (type) {
        IncidentTypeEnum.COLLISION -> listOf(
            "Review and update safety procedures",
            "Additional operator training",
            "Improve visibility in area",
            "Install warning signals/signs"
        )
        IncidentTypeEnum.NEAR_MISS -> listOf(
            "Review safety protocols",
            "Update risk assessment",
            "Additional safety equipment",
            "Improve communication procedures"
        )
        IncidentTypeEnum.HAZARD -> listOf(
            "Remove or isolate hazard",
            "Install protective barriers",
            "Update warning signage",
            "Modify work procedures"
        )
        IncidentTypeEnum.VEHICLE_FAIL -> listOf(
            "Review maintenance schedule",
            "Update inspection procedures",
            "Replace faulty components",
            "Operator training on vehicle checks"
        )
        null -> emptyList()
    }
} 