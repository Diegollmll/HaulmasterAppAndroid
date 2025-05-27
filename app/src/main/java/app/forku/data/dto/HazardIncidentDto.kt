package app.forku.data.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class HazardIncidentDto(
    // Campos particulares de HazardIncident
    @SerializedName("CorrectiveActions")
    val correctiveActions: List<Int>? = null,
    @SerializedName("HazardType")
    val hazardType: Int? = null,
    @SerializedName("PotentialConsequences")
    val potentialConsequences: List<Int>? = null,
    @SerializedName("PreventiveMeasures")
    val preventiveMeasures: List<Int>? = null,
    // Campos comunes de IncidentDto
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("Description")
    val description: String,
    @SerializedName("GOUserId")
    val userId: String,
    @SerializedName("IncidentDateTime")
    val incidentDateTime: String,
    @SerializedName("IncidentType")
    val incidentType: Int,
    @SerializedName("LocationDetails")
    val locationDetails: String,
    @SerializedName("SeverityLevel")
    val severityLevel: Int,
    @SerializedName("Status")
    val status: Int,
    @SerializedName("Weather")
    val weather: String? = null,
    @SerializedName("IsDirty")
    val isDirty: Boolean = true,
    @SerializedName("IsNew")
    val isNew: Boolean = true,
    @SerializedName("IsMarkedForDeletion")
    val isMarkedForDeletion: Boolean = false,
    @SerializedName("VehicleId")
    val vehicleId: String? = null,
    @SerializedName("LocationCoordinates")
    val locationCoordinates: String? = null,
    @SerializedName("IsLoadCarried")
    val isLoadCarried: Boolean? = null,
    @SerializedName("LoadBeingCarried")
    val loadBeingCarried: String? = null,
    @SerializedName("LoadWeight")
    val loadWeight: Int? = null,
    @SerializedName("OthersInvolved")
    val othersInvolved: String? = null
) 