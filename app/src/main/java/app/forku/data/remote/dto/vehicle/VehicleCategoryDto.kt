package app.forku.data.remote.dto.vehicle

import app.forku.domain.model.vehicle.VehicleCategoryModel
import com.google.gson.annotations.SerializedName

data class VehicleCategoryDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("requires_certification")
    val requiresCertification: Boolean,
    
    @SerializedName("created_at")
    val createdAt: Long,
    
    @SerializedName("updated_at")
    val updatedAt: Long
) {
    fun toModel() = VehicleCategoryModel(
        id = id,
        name = name,
        description = description,
        requiresCertification = requiresCertification,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
} 