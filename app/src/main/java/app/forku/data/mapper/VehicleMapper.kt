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
import app.forku.data.mapper.toDomain
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import app.forku.data.mapper.VehicleSessionMapper

fun VehicleDto.toDomain(): Vehicle {
    Log.d("VehicleMapper", "Mapping VehicleDto to Vehicle: id=${id}, codename=${codename}, businessId=${businessId}, siteId=${siteId}, vehicleTypeId=${vehicleTypeId}, categoryId=${categoryId}")
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

    val vehicleType = vehicleType?.toDomain() ?: VehicleType.createPlaceholder(
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
        energySource = energySource ?: 1,
        energySourceDisplayString = null,
        nextService = nextServiceHours,
        currentHourMeter = currentHourMeter,
        businessId = businessId,
        siteId = siteId,
        isDirty = isDirty,
        isNew = isNew,
        isMarkedForDeletion = isMarkedForDeletion
    )
}


fun Vehicle.toDto(): VehicleDto {
    // 🔍 LOG CRÍTICO: Verificar estado de la imagen ANTES de la conversión
    android.util.Log.d("VehicleMapper", """
        🔍 IMAGEN DEL VEHÍCULO EN DOMAIN (toDto):
        - Vehicle ID: $id
        - Codename: $codename
        - photoModel: $photoModel
        - Status: $status
        - Business ID: $businessId
        - Site ID: $siteId
    """.trimIndent())
    
    val dto = VehicleDto(
        id = id,
        idOldValue = id, // Set old value to current id for updates
        vehicleTypeId = if (vehicleTypeId.isNotEmpty()) vehicleTypeId else type.Id,
        categoryId = categoryId,
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
        energySource = energySource,
        nextServiceDateTime = null, // TODO: Convert from hours if needed
        currentHourMeter = currentHourMeter,
        businessId = businessId,
        siteId = siteId,
        isMarkedForDeletion = isMarkedForDeletion,
        isDirty = isDirty,
        isNew = isNew,
        // Set new object IDs to null
        businessNewObjectId = null,
        siteNewObjectId = null,
        vehicleCategoryNewObjectId = null,
        vehicleTypeNewObjectId = null
    )
    
    // 🔍 LOG CRÍTICO: Verificar estado de la imagen DESPUÉS de la conversión
    android.util.Log.d("VehicleMapper", """
        🔍 IMAGEN DEL VEHÍCULO EN DTO RESULTANTE:
        - Vehicle ID: ${dto.id}
        - Codename: ${dto.codename}
        - photoModel: ${dto.photoModel}
        - pictureFileSize: ${dto.pictureFileSize}
        - pictureInternalName: ${dto.pictureInternalName}
        - Status: ${dto.status}
        - Business ID: ${dto.businessId}
        - Site ID: ${dto.siteId}
    """.trimIndent())
    
    return dto
}

fun VehicleDto.toFormMap(): Map<String, Any> {
    // 🔍 LOG CRÍTICO: Verificar estado de la imagen ANTES de la conversión a FormMap
    android.util.Log.d("VehicleMapper", """
        🔍 IMAGEN DEL VEHÍCULO EN DTO (toFormMap):
        - Vehicle ID: $id
        - Codename: $codename
        - photoModel: $photoModel
        - pictureFileSize: $pictureFileSize
        - pictureInternalName: $pictureInternalName
        - Status: $status
        - Business ID: $businessId
        - Site ID: $siteId
    """.trimIndent())
    
    val map = mutableMapOf<String, Any?>()
    
            // Required fields that cannot be null
    map["Id"] = id ?: ""
    map["BusinessId"] = businessId ?: ""
    map["VehicleTypeId"] = vehicleTypeId
    map["VehicleCategoryId"] = categoryId
    map["Status"] = status
    map["Codename"] = codename ?: ""
    map["Model"] = model ?: ""
    map["EnergySource"] = energySource ?: 1
    
    // Optional fields that can be omitted if null
    if (siteId != null) map["SiteId"] = siteId
    if (serialNumber != null) map["SerialNumber"] = serialNumber
    if (description != null) map["Description"] = description
    if (bestSuitedFor != null) map["BestSuitedFor"] = bestSuitedFor
    if (photoModel != null) map["Picture"] = photoModel
    if (pictureFileSize != null) map["PictureFileSize"] = pictureFileSize
    if (pictureInternalName != null) map["PictureInternalName"] = pictureInternalName
    if (nextServiceDateTime != null) map["NextServiceDateTime"] = nextServiceDateTime
    if (nextServiceDateTimeWithTimezoneOffset != null) map["NextServiceDateTime_WithTimezoneOffset"] = nextServiceDateTimeWithTimezoneOffset
    if (currentHourMeter != null) map["CurrentHourMeter"] = currentHourMeter
    
    // Campos booleanos
    map["IsMarkedForDeletion"] = isMarkedForDeletion
    
    // 🔍 LOG CRÍTICO: Verificar estado de la imagen DESPUÉS de la conversión a FormMap
    android.util.Log.d("VehicleMapper", """
        🔍 IMAGEN DEL VEHÍCULO EN FORMMAP RESULTANTE:
        - Vehicle ID: ${map["Id"]}
        - Codename: ${map["Codename"]}
        - Picture field: ${map["Picture"]}
        - PictureFileSize field: ${map["PictureFileSize"]}
        - PictureInternalName field: ${map["PictureInternalName"]}
        - Total fields: ${map.size}
        - All fields: ${map.keys.joinToString(", ")}
    """.trimIndent())
    
    // Filtrar cualquier valor nulo que quede y convertir a Map<String, Any>
    return map.filterValues { it != null } as Map<String, Any>
}

fun GetVehiclePlaceholder():Vehicle {
    return Vehicle(
        id = "",
        codename = "",
        model = "",
        type = VehicleType.createPlaceholder("","",false,""),
        categoryId = "",
        status = VehicleStatus.AVAILABLE,
        serialNumber = "",
        description = "",
        bestSuitedFor = "",
        photoModel = "",
        energyType = "",
        energySource = 1,
        energySourceDisplayString = null,
        nextService = "",
        currentHourMeter = "",
        businessId = "",
        siteId = "",
        isDirty = false,
        isNew = true,
        isMarkedForDeletion = false
    )
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
            "ELECTRIC" -> 0  // ✅ Fixed: Backend Electric = 0
            "DIESEL" -> 2    // ✅ Correct: Backend Diesel = 2  
            "LPG", "GAS" -> 1  // ✅ Fixed: Backend Lpg = 1
            else -> 0        // ✅ Default to Electric
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

fun VehicleDto.toDomainWithIncludedData(): VehicleWithSessionAndOperatorData {
    android.util.Log.d("VehicleMapper", "=== Mapping VehicleDto with included data ===")
    android.util.Log.d("VehicleMapper", "Vehicle ID: $id")
    android.util.Log.d("VehicleMapper", "VehicleSessionItems count: ${vehicleSessionItems?.size ?: 0}")
    android.util.Log.d("VehicleMapper", "ChecklistAnswerItems count: ${checklistAnswerItems?.size ?: 0}")
    
    // Map the base vehicle
    val vehicle = this.toDomain()
    
    // Process VehicleSessionItems to find active and last sessions
    val sessions = vehicleSessionItems?.map { sessionDto ->
        android.util.Log.d("VehicleMapper", "Processing session: ${sessionDto.Id}, Status: ${sessionDto.Status}, UserId: ${sessionDto.GOUserId}")
        VehicleSessionMapper.toDomain(sessionDto)
    } ?: emptyList()
    
    // Find active session (Status = 0 means OPERATING)
    val activeSession = sessions.find { session ->
        session.status == app.forku.domain.model.session.VehicleSessionStatus.OPERATING
    }
    android.util.Log.d("VehicleMapper", "Active session found: ${activeSession?.id}")
    
    // Find last completed session if no active session
    val lastSession = if (activeSession == null) {
        sessions
            .filter { it.status == app.forku.domain.model.session.VehicleSessionStatus.NOT_OPERATING && it.endTime != null }
            .maxByOrNull { it.endTime!! }
    } else null
    android.util.Log.d("VehicleMapper", "Last session found: ${lastSession?.id}")
    
    // Process ChecklistAnswerItems to find last checklist
    val checklistAnswers = checklistAnswerItems?.map { checklistDto ->
        android.util.Log.d("VehicleMapper", "Processing checklist: ${checklistDto.id}, Status: ${checklistDto.status}, UserId: ${checklistDto.goUserId}, LastCheckDateTime: ${checklistDto.lastCheckDateTime}, EndDateTime: ${checklistDto.endDateTime}")
        checklistDto.toDomain()
    } ?: emptyList()
    
    val lastChecklistAnswer = checklistAnswers
        .sortedByDescending { it.lastCheckDateTime.takeIf { it.isNotBlank() } ?: it.endDateTime }
        .firstOrNull()
    android.util.Log.d("VehicleMapper", "Last checklist answer found: ${lastChecklistAnswer?.id}, Status: ${lastChecklistAnswer?.status}, DateTime: ${lastChecklistAnswer?.lastCheckDateTime}")
    
    return VehicleWithSessionAndOperatorData(
        vehicle = vehicle,
        activeSession = activeSession,
        lastSession = lastSession,
        lastChecklistAnswer = lastChecklistAnswer
    )
}

data class VehicleWithSessionAndOperatorData(
    val vehicle: app.forku.domain.model.vehicle.Vehicle,
    val activeSession: app.forku.domain.model.session.VehicleSession?,
    val lastSession: app.forku.domain.model.session.VehicleSession?,
    val lastChecklistAnswer: app.forku.domain.model.checklist.ChecklistAnswer?
)

