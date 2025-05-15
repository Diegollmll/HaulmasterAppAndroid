package app.forku.data.mapper

import app.forku.data.api.dto.vehicle.VehicleDto
import app.forku.data.api.dto.vehicle.VehicleTypeDto
import app.forku.domain.model.vehicle.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max
import android.util.Log
import app.forku.data.api.dto.vehicle.UpdateVehicleDto
import app.forku.data.api.dto.vehicle.VehicleObjectData
import app.forku.data.api.dto.vehicle.ObjectsDataSet
import app.forku.data.api.dto.vehicle.VehicleObjectsDataSet
import app.forku.data.mapper.toDomain as vehicleTypeDtoToDomain // Use the extension from VehicleTypeMapper
import com.google.gson.JsonObject
import com.google.gson.JsonArray

fun VehicleDto.toDomain(): Vehicle {
    val nextServiceHours = try {
        if (nextServiceDateTime != null) {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val nextServiceDate = LocalDateTime.parse(nextServiceDateTime, formatter)
            val now = LocalDateTime.now()
            val hours = ChronoUnit.HOURS.between(now, nextServiceDate)
            max(0, hours).toString()
        } else {
            "0"
        }
    } catch (e: Exception) {
        "0"
    }

    // VehicleType mapping should be handled via VehicleTypeMapper if DTO is available
    // If only IDs are available, use a placeholder
    val vehicleType = VehicleType.createPlaceholder(
        Id = vehicleTypeId ?: "",
        Name = "Unknown",
        RequiresCertification = false,
        VehicleCategoryId = categoryId ?: "",
        IsMarkedForDeletion = false,
        InternalObjectId = 0
    )

    return Vehicle(
        id = id ?: "",
        codename = codename ?: "",
        model = model ?: "",
        type = vehicleType,
        categoryId = categoryId ?: "",
        status = VehicleStatus.fromInt(status),
        serialNumber = serialNumber ?: "",
        description = description ?: "",
        bestSuitedFor = bestSuitedFor ?: "",
        photoModel = photoModel ?: "",
        energyType = energySource?.let { EnergySourceEnum.fromInt(it).toString() } ?: "",
        nextService = nextServiceHours,
        businessId = businessId
    )
}


fun Vehicle.toDto(): VehicleDto {
    return VehicleDto(
        id = id,
        idOldValue = id, // Set old value to current id for updates
        vehicleTypeId = type.Id,
        categoryId = type.VehicleCategoryId,
        status = when(status) {
            VehicleStatus.AVAILABLE -> 1
            VehicleStatus.IN_USE -> 2
            VehicleStatus.OUT_OF_SERVICE -> 3
            VehicleStatus.MAINTENANCE -> 4
            else -> 3
        },
        serialNumber = serialNumber,
        description = description,
        bestSuitedFor = bestSuitedFor,
        photoModel = photoModel,
        codename = codename,
        model = model,
        energySource = when(energyType.uppercase()) {
            "ELECTRIC" -> 1
            "DIESEL" -> 2
            "GAS" -> 3
            else -> 1
        },
        nextServiceDateTime = null, // TODO: Convert from hours if needed
        businessId = businessId,
        // Set new object IDs to null
        businessNewObjectId = null,
        siteNewObjectId = null,
        vehicleCategoryNewObjectId = null,
        vehicleTypeNewObjectId = null
    )
}

fun VehicleDto.toFormMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any?>()
    
    // Campos requeridos que no pueden ser nulos
    map["Id"] = id ?: ""
    map["BusinessId"] = businessId ?: ""
    map["VehicleTypeId"] = vehicleTypeId
    map["VehicleCategoryId"] = categoryId
    map["Status"] = status
    map["Codename"] = codename ?: ""
    map["Model"] = model ?: ""
    map["EnergySource"] = energySource ?: 1
    
    // Campos opcionales que pueden ser omitidos si son nulos
    if (siteId != null) map["SiteId"] = siteId
    if (serialNumber != null) map["SerialNumber"] = serialNumber
    if (description != null) map["Description"] = description
    if (bestSuitedFor != null) map["BestSuitedFor"] = bestSuitedFor
    if (photoModel != null) map["Picture"] = photoModel
    if (pictureFileSize != null) map["PictureFileSize"] = pictureFileSize
    if (pictureInternalName != null) map["PictureInternalName"] = pictureInternalName
    if (nextServiceDateTime != null) map["NextServiceDateTime"] = nextServiceDateTime
    if (nextServiceDateTimeWithTimezoneOffset != null) map["NextServiceDateTime_WithTimezoneOffset"] = nextServiceDateTimeWithTimezoneOffset
    
    // Campos booleanos
    map["IsMarkedForDeletion"] = isMarkedForDeletion
    
    // Filtrar cualquier valor nulo que quede y convertir a Map<String, Any>
    return map.filterValues { it != null } as Map<String, Any>
}

fun Vehicle.toUpdateDto(internalObjectId: Int = 4): UpdateVehicleDto {
    val vehicleData = VehicleObjectData(
        id = id,
        idOldValue = id,
        businessId = businessId ?: "",
        businessIdOldValue = businessId ?: "",
        codename = codename,
        model = model,
        description = description,
        bestSuitedFor = bestSuitedFor,
        picture = photoModel,
        serialNumber = serialNumber,
        status = status.toInt(),
        energySource = when(energyType.uppercase()) {
            "ELECTRIC" -> 1
            "DIESEL" -> 2
            "GAS" -> 3
            else -> 1
        },
        vehicleTypeId = type.Id,
        vehicleTypeIdOldValue = type.Id,
        vehicleCategoryId = type.VehicleCategoryId,
        vehicleCategoryIdOldValue = type.VehicleCategoryId,
        internalObjectId = internalObjectId
    )

    return UpdateVehicleDto(
        internalObjectId = internalObjectId,
        primaryKey = id,
        objectsDataSet = ObjectsDataSet(
            vehicleObjectsDataSet = VehicleObjectsDataSet(
                vehicleObjects = mapOf(
                    internalObjectId.toString() to vehicleData
                )
            )
        )
    )
}

fun VehicleDto.toJsonObject(newStatus: VehicleStatus? = null): JsonObject {
    return JsonObject().apply {
        // Add type information first
        addProperty("\$type", "VehicleDataObject")

        // Required fields - ensure no nulls
        addProperty("Id", id ?: "")
        addProperty("Id_OldValue", id ?: "")
        addProperty("BusinessId", businessId ?: "")
        addProperty("BusinessId_OldValue", businessId ?: "")
        addProperty("Codename", codename ?: "")
        addProperty("Model", model ?: "")
        addProperty("Description", description?.replace(" ", "+") ?: "")
        addProperty("BestSuitedFor", bestSuitedFor?.replace(" ", "+") ?: "")
        addProperty("SerialNumber", serialNumber ?: "")
        addProperty("Status", newStatus?.toInt() ?: status)
        addProperty("VehicleCategoryId", categoryId ?: "")
        addProperty("VehicleTypeId", vehicleTypeId ?: "")
        addProperty("EnergySource", energySource ?: 1)
        addProperty("Picture", photoModel ?: "")
        addProperty("PictureFileSize", pictureFileSize ?: 0)
        
        // Null fields using JsonNull
        add("SiteId", com.google.gson.JsonNull.INSTANCE)
        add("_business_NewObjectId", com.google.gson.JsonNull.INSTANCE)
        add("_site_NewObjectId", com.google.gson.JsonNull.INSTANCE)
        add("_vehicleCategory_NewObjectId", com.google.gson.JsonNull.INSTANCE)
        add("_vehicleType_NewObjectId", com.google.gson.JsonNull.INSTANCE)
        add("NextServiceDateTime", com.google.gson.JsonNull.INSTANCE)
        add("NextServiceDateTime_WithTimezoneOffset", com.google.gson.JsonNull.INSTANCE)
    }
}

