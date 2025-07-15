package app.forku.domain.model.checklist

import app.forku.domain.model.vehicle.EnergySourceEnum
import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.model.vehicle.VehicleComponentEnum

data class ChecklistItem(
    val id: String,
    val checklistId: String,
    val version: String = "1.0", // ✅ NEW: Version of this specific question
    val category: String,
    val subCategory: String,
    val energySourceEnum: List<EnergySourceEnum>,
    val vehicleType: List<VehicleType>,
    val component: VehicleComponentEnum,
    val question: String,
    val description: String,
    val isCritical: Boolean,
    val expectedAnswer: Answer,
    val rotationGroup: Int,
    val userAnswer: Answer? = null,
    val userComment: String? = null,
    val supportedVehicleTypeIds: Set<String> = emptySet(), // ✅ New: IDs of vehicle types this question supports
    val goUserId: String? = null, // ✅ New: ID of the user who created this question
    val allVehicleTypesEnabled: Boolean = false, // ✅ New: Whether this question applies to all vehicle types
    val createdAt: String? = null, // ✅ NEW: When this question version was created
    val modifiedAt: String? = null // ✅ NEW: When this question version was last modified
)
