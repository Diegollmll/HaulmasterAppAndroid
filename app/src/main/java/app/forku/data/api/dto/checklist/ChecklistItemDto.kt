package app.forku.data.api.dto.checklist

import app.forku.domain.model.checklist.Answer

data class ChecklistItemDto(
    val id: String,
    val category: String,
    val subCategory: String,
    val energySource: List<String>,
    val vehicleType: List<String>,
    val component: String,
    val question: String,
    val description: String,
    val isCritical: Boolean,
    val expectedAnswer: String,
    val rotationGroup: Int,
    val userAnswer: Answer? = null
) 