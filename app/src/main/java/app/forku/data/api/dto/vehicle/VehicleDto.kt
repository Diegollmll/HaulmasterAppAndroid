package app.forku.data.api.dto.vehicle

import app.forku.data.api.dto.checklist.PreShiftCheckDto
import app.forku.domain.model.vehicle.VehicleStatus
import com.google.gson.annotations.SerializedName

data class VehicleDto(
    @SerializedName("Id")
    val id: String? = null,  // Allow null for creation
    
    @SerializedName("BusinessId")
    val businessId: String? = null,
    
    @SerializedName("VehicleTypeId")
    val vehicleTypeId: String,
    
    @SerializedName("CategoryId")
    val categoryId: String,
    
    @SerializedName("Status")
    val status: String = "AVAILABLE",
    
    @SerializedName("SerialNumber")
    val serialNumber: String = "",
    
    @SerializedName("Description")
    val description: String,
    
    @SerializedName("BestSuitedFor")
    val bestSuitedFor: String,
    
    @SerializedName("PhotoModel")
    val photoModel: String,
    
    @SerializedName("Codename")
    val codename: String,
    
    @SerializedName("Model")
    val model: String,
    
    @SerializedName("EnergyType")
    val energyType: String,
    
    @SerializedName("NextService")
    val nextService: String
)
