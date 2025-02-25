package app.forku.domain.model.checklist

import app.forku.domain.model.vehicle.EnergySource

data class ChecklistMetadata(
    val version: String,
    val lastUpdated: String,
    val totalQuestions: Int,
    val rotationGroups: Int,
    val questionsPerCheck: Int,
    val energySources: List<EnergySource>
)