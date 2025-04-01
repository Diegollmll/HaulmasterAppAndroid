package app.forku.data.model

import com.google.gson.annotations.SerializedName

data class VehicleSessionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("vehicle_id")
    val vehicleId: String,
    @SerializedName("operator_id")
    val operatorId: String,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String?,
    @SerializedName("close_method")
    val closeMethod: String? = null,
    @SerializedName("closed_by")
    val closedBy: String? = null,
    @SerializedName("start_location")
    val startLocation: String? = null,
    @SerializedName("end_location")
    val endLocation: String? = null,
    @SerializedName("start_location_coordinates")
    val startLocationCoordinates: String? = null,
    @SerializedName("end_location_coordinates")
    val endLocationCoordinates: String? = null
) 