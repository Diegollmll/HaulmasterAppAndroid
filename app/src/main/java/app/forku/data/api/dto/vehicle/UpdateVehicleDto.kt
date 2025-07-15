package app.forku.data.api.dto.vehicle

import com.google.gson.annotations.SerializedName

data class UpdateVehicleDto(
    @SerializedName("InternalObjectId")
    val internalObjectId: Int,
    
    @SerializedName("PrimaryKey")
    val primaryKey: String,
    
    @SerializedName("ObjectsDataSet")
    val objectsDataSet: ObjectsDataSet
)

data class ObjectsDataSet(
    @SerializedName("\$type")
    val type: String = "ObjectsDataSet",
    
    @SerializedName("VehicleObjectsDataSet")
    val vehicleObjectsDataSet: VehicleObjectsDataSet
)

data class VehicleObjectsDataSet(
    @SerializedName("VehicleObjects")
    val vehicleObjects: Map<String, VehicleObjectData>
)

data class VehicleObjectData(
    @SerializedName("Id")
    val id: String,
    
    @SerializedName("Id_OldValue")
    val idOldValue: String,
    
    @SerializedName("_business_NewObjectId")
    val businessNewObjectId: String? = null,
    
    @SerializedName("_site_NewObjectId")
    val siteNewObjectId: String? = null,
    
    @SerializedName("_vehicleCategory_NewObjectId")
    val vehicleCategoryNewObjectId: String? = null,
    
    @SerializedName("_vehicleType_NewObjectId")
    val vehicleTypeNewObjectId: String? = null,
    
    @SerializedName("BestSuitedFor")
    val bestSuitedFor: String,
    
    @SerializedName("BusinessId")
    val businessId: String,
    
    @SerializedName("Codename")
    val codename: String,
    
    @SerializedName("Description")
    val description: String,
    
    @SerializedName("EnergySource")
    val energySource: Int,
    
    @SerializedName("Model")
    val model: String,
    
    @SerializedName("NextServiceDateTime")
    val nextServiceDateTime: String? = null,
    
    @SerializedName("CurrentHourMeter")
    val currentHourMeter: String? = null,
    
    @SerializedName("Picture")
    val picture: String,
    
    @SerializedName("PictureFileSize")
    val pictureFileSize: Long? = null,
    
    @SerializedName("PictureInternalName")
    val pictureInternalName: String? = null,
    
    @SerializedName("SerialNumber")
    val serialNumber: String,
    
    @SerializedName("SiteId")
    val siteId: String? = null,
    
    @SerializedName("Status")
    val status: Int,
    
    @SerializedName("VehicleCategoryId")
    val vehicleCategoryId: String,
    
    @SerializedName("VehicleTypeId")
    val vehicleTypeId: String,
    
    @SerializedName("BusinessId_OldValue")
    val businessIdOldValue: String,
    
    @SerializedName("SiteId_OldValue")
    val siteIdOldValue: String? = null,
    
    @SerializedName("VehicleCategoryId_OldValue")
    val vehicleCategoryIdOldValue: String,
    
    @SerializedName("VehicleTypeId_OldValue")
    val vehicleTypeIdOldValue: String,
    
    @SerializedName("InternalObjectId")
    val internalObjectId: Int,
    
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    
    @SerializedName("IsNew")
    val isNew: Boolean = false,
    
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false
) 