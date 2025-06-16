package app.forku.domain.model.vehicle

import app.forku.domain.model.checklist.PreShiftCheck
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Vehicle(
    val id: String = "",
    val codename: String = "",
    val type: VehicleType,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val model: String = "",
    val serialNumber: String = "",
    val description: String = "",
    val bestSuitedFor: String = "",
    val photoModel: String = "",
    val categoryId: String = "",
    val vehicleTypeId: String = "",
    val energyType: String = "",
    val energySource: Int = 1,
    val energySourceDisplayString: String? = null,
    val nextService: String = "",
    val hasIssues: Boolean = false,
    val businessId: String? = null,
    val siteId: String? = null,
    val isDirty: Boolean = true,
    val isNew: Boolean = true,
    val isMarkedForDeletion: Boolean = false
) {

    fun getPictureUrl(baseUrl: String, lastEditedTime: String? = "%LASTEDITEDTIME%"): String {
        val timeParam = lastEditedTime?.let { "?t=$it" } ?: ""
        val url = "$baseUrl" + "api/vehicle/file/$id/Picture$timeParam"
        // android.util.Log.d("VehicleImage", "Generated vehicle image URL: $url")
        return url
    }
}

enum class MaintenanceStatus {
    UP_TO_DATE,
    DUE_SOON,
    OVERDUE
}

