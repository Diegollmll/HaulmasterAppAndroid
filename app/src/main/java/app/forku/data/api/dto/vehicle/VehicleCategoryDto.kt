package app.forku.data.api.dto.vehicle

import app.forku.domain.model.vehicle.VehicleCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.util.Log

@Serializable
data class VehicleCategoryDto(
    @SerialName("\$type")
    val type: String = "VehicleCategoryDataObject",
    
    @SerialName("Id")
    val id: String? = null,
    
    @SerialName("Name")
    val name: String,
    
    @SerialName("Description")
    val description: String? = null,
    
    @SerialName("RequiresCertification")
    val requiresCertification: Boolean = false,
    
    @SerialName("IsDirty")
    val isDirty: Boolean = true,
    
    @SerialName("IsNew")
    val isNew: Boolean = true,
    
    @SerialName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerialName("InternalObjectId")
    val internalObjectId: Int = 0
)

 