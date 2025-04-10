package app.forku.data.api.dto.vehicle

import app.forku.data.api.dto.checklist.PreShiftCheckDto
import app.forku.domain.model.vehicle.VehicleStatus
import com.google.gson.annotations.SerializedName

data class VehicleDto(
    @SerializedName("id")
    val id: String? = null,  // Allow null for creation
    
    @SerializedName("businessId")
    val businessId: String? = null,
    
    @SerializedName("vehicleTypeId")
    val vehicleTypeId: String,
    
    @SerializedName("categoryId")
    val categoryId: String,
    
    @SerializedName("status")
    val status: String = "AVAILABLE",
    
    @SerializedName("serialNumber")
    val serialNumber: String = "",
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("bestSuitedFor")
    val bestSuitedFor: String,
    
    @SerializedName("photoModel")
    val photoModel: String,
    
    @SerializedName("codename")
    val codename: String,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("energyType")
    val energyType: String,
    
    @SerializedName("nextService")
    val nextService: String
)
