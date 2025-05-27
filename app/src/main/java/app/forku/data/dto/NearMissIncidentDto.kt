package app.forku.data.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class NearMissIncidentDto(
    @SerializedName("ContributingFactors")
    val contributingFactors: List<Int>? = null,
    @SerializedName("ImmediateActions")
    val immediateActions: List<Int>? = null,
    @SerializedName("ImmediateCause")
    val immediateCause: List<Int>? = null,
    @SerializedName("IsLoadCarried")
    val isLoadCarried: Boolean? = null,
    @SerializedName("LoadWeight")
    val loadWeight: Int? = null,
    @SerializedName("LoadBeingCarried")
    val loadBeingCarried: String? = null,
    @SerializedName("LongTermSolutions")
    val longTermSolutions: List<Int>? = null,
    @SerializedName("NearMissType")
    val nearMissType: List<Int>? = null,
    @SerializedName("OthersInvolved")
    val othersInvolved: String? = null,
    @SerializedName("VehicleId")
    val vehicleId: String? = null,
    // IncidentDto fields
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
    @SerializedName("LocationCoordinates")
    val locationCoordinates: String? = null,
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
    val isMarkedForDeletion: Boolean = false
) 