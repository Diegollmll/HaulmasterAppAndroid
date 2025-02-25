package app.forku.data.api.dto.checklist

data class ChecklistItemDto(
    val id: String,
    val category: String,
    val subCategory: String,
    val energySource: List<String>,
    val vehicleType: List<String>,
    val component: String,
    val question: String,
    val description: String,
    val criticality: String,
    val expectedAnswer: String,
    val rotationGroup: Int
) 