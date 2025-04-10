package app.forku.data.api.dto.vehicle

import app.forku.domain.model.vehicle.VehicleType
import com.google.gson.annotations.SerializedName

data class VehicleTypeDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("categoryId")
    val categoryId: String? = null,
    
    @SerializedName("maxWeight")
    val maxWeight: Double? = null,
    
    @SerializedName("maxPassengers")
    val maxPassengers: Int? = null,
    
    @SerializedName("requiresSpecialLicense")
    val requiresSpecialLicense: Boolean = false,
    
    @SerializedName("requiresCertification")
    val requiresCertification: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: Long,
    
    @SerializedName("updatedAt")
    val updatedAt: Long
) {
    fun toModel() = VehicleType(
        id = id,
        name = name,
        description = description,
        categoryId = categoryId ?: "",
        maxWeight = maxWeight,
        maxPassengers = maxPassengers,
        requiresSpecialLicense = requiresSpecialLicense,
        requiresCertification = requiresCertification,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun VehicleType.toDto() = VehicleTypeDto(
    id = id,
    name = name,
    description = description,
    categoryId = categoryId.takeIf { it.isNotBlank() },
    maxWeight = maxWeight,
    maxPassengers = maxPassengers,
    requiresSpecialLicense = requiresSpecialLicense,
    requiresCertification = requiresCertification,
    createdAt = createdAt,
    updatedAt = updatedAt
) 