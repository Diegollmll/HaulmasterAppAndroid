package app.forku.data.remote.dto

import app.forku.domain.model.vehicle.VehicleCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleCategoryDto(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("created_at")
    val createdAt: Long,
    
    @SerialName("updated_at")
    val updatedAt: Long,
    
    @SerialName("requires_certification")
    val requiresCertification: Boolean = false
)

fun VehicleCategoryDto.toDomain(): VehicleCategory = VehicleCategory(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    requiresCertification = requiresCertification
)

fun VehicleCategory.toDto(): VehicleCategoryDto = VehicleCategoryDto(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    requiresCertification = requiresCertification
)

fun List<VehicleCategoryDto>.toDomainModel(): List<VehicleCategory> {
    return map { it.toDomain() }
} 