package app.forku.data.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import app.forku.data.api.dto.incident.IncidentDto

@Serializable
data class CollisionIncidentDto(
    @SerializedName("InjurySeverity")
    val injurySeverity: Int? = null,
    @SerializedName("InjuryLocation")
    val injuryLocations: List<Int>? = null,
    @SerializedName("DamageOccurrence")
    val damageOccurrence: List<Int>? = null,
    @SerializedName("CollisionType")
    val collisionType: List<Int>? = null,
    @SerializedName("CommonCauses")
    val commonCauses: List<Int>? = null,
    @SerializedName("ContributingFactors")
    val contributingFactors: List<Int>? = null,
    @SerializedName("ImmediateAction")
    val immediateActions: List<Int>? = null,
    @SerializedName("ImmediateCause")
    val immediateCauses: List<Int>? = null,
    @SerializedName("LongTermSolution")
    val longTermSolutions: List<Int>? = null,
    @SerializedName("EnvironmentalImpact")
    val environmentalImpact: List<Int>? = null,
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("IncidentDateTime")
    val incidentDateTime: String,
    @SerializedName("Description")
    val description: String,
    @SerializedName("GOUserId")
    val userId: String,
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
    val othersInvolved: String? = null,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null
)