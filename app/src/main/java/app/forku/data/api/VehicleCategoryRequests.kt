package app.forku.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateVehicleCategoryRequest(
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("requires_certification")
    val requiresCertification: Boolean = false
)

@Serializable
data class UpdateVehicleCategoryRequest(
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("requires_certification")
    val requiresCertification: Boolean = false
) 