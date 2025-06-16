package app.forku.data.api.dto.vehicle

import app.forku.domain.model.vehicle.VehicleCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.util.Log

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

fun VehicleCategoryDto.toDomain(): VehicleCategory? {
    if (id.isBlank()) {
        Log.e("VehicleCategoryMapper", "VehicleCategoryDto.id is null or blank! Data: $this")
        return null
    }
    return VehicleCategory(
        id = id,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        requiresCertification = requiresCertification
    )
}

fun VehicleCategory.toDto(): VehicleCategoryDto = VehicleCategoryDto(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    requiresCertification = requiresCertification
) 