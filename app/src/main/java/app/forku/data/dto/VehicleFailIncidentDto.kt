package app.forku.data.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleFailIncidentDto(
    // Campos particulares de VehicleFailIncident
    @SerializedName("ContributingFactors")
    val contributingFactors: List<Int>? = null,
    @SerializedName("DamageOccurrence")
    val damageOccurrence: List<Int>? = null,
    @SerializedName("EnvironmentalImpact")
    val environmentalImpact: List<Int>? = null,
    @SerializedName("FailImmediateCause")
    val failImmediateCause: List<Int>? = null,
    @SerializedName("FailureType")
    val failureType: List<Int>? = null,
    @SerializedName("IsLoadCarried")
    val isLoadCarried: Boolean? = null,
    @SerializedName("LoadBeingCarried")
    val loadBeingCarried: String? = null,
    @SerializedName("LoadWeight")
    val loadWeight: Int? = null,
    @SerializedName("LongTermSolution")
    val longTermSolution: List<Int>? = null,
    @SerializedName("VehicleFailImmediateAction")
    val vehicleFailImmediateAction: List<Int>? = null,
    @SerializedName("VehicleId")
    val vehicleId: String? = null,
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
    @SerializedName("LocationCoordinates")
    val locationCoordinates: String? = null,
    @SerializedName("OthersInvolved")
    val othersInvolved: String? = null,
    @SerializedName("BusinessId")
    val businessId: String? = null,
    @SerializedName("SiteId")
    val siteId: String? = null
) 