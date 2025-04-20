package app.forku.domain.model.checklist

import app.forku.domain.model.vehicle.EnergySourceEnum
import app.forku.domain.model.vehicle.VehicleType

data class ChecklistItem(
    val id: String,
    val category: String,
    val subCategory: String,
    val energySourceEnum: List<EnergySourceEnum>,
    val vehicleType: List<VehicleType>,
    val component: String,
    val question: String,
    val description: String,
    val isCritical: Boolean,
    val expectedAnswer: Answer,
    val rotationGroup: Int,
    val userAnswer: Answer? = null
)
