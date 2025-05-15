package app.forku.data.api.dto.vehicle

import com.google.gson.annotations.SerializedName

data class VehicleDto(
    @SerializedName("Id")
    val id: String? = null,
    
    @SerializedName("BusinessId")
    val businessId: String? = null,
    
    @SerializedName("SiteId")
    val siteId: String? = null,
    
    @SerializedName("VehicleTypeId")
    val vehicleTypeId: String,
    
    @SerializedName("VehicleCategoryId")
    val categoryId: String,
    
    @SerializedName("Status")
    val status: Int = 1,
    
    @SerializedName("SerialNumber")
    val serialNumber: String = "",
    
    @SerializedName("Description")
    val description: String,
    
    @SerializedName("BestSuitedFor")
    val bestSuitedFor: String,
    
    @SerializedName("Picture")
    val photoModel: String,
    
    @SerializedName("PictureFileSize")
    val pictureFileSize: Long? = null,
    
    @SerializedName("PictureInternalName")
    val pictureInternalName: String? = null,
    
    @SerializedName("Codename")
    val codename: String,
    
    @SerializedName("Model")
    val model: String,
    
    @SerializedName("EnergySource")
    val energySource: Int = 1,
    
    @SerializedName("NextServiceDateTime")
    val nextServiceDateTime: String? = null,
    
    @SerializedName("NextServiceDateTime_WithTimezoneOffset")
    val nextServiceDateTimeWithTimezoneOffset: String? = null,
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int? = null,
    
    @SerializedName("\$type")
    val type: String = "VehicleDataObject",
    
    @SerializedName("Id_OldValue")
    val idOldValue: String? = null,
    
    @SerializedName("_business_NewObjectId")
    val businessNewObjectId: String? = null,
    
    @SerializedName("_site_NewObjectId")
    val siteNewObjectId: String? = null,
    
    @SerializedName("_vehicleCategory_NewObjectId")
    val vehicleCategoryNewObjectId: String? = null,
    
    @SerializedName("_vehicleType_NewObjectId")
    val vehicleTypeNewObjectId: String? = null
)
