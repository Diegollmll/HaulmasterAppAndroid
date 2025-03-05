package app.forku.domain.model.vehicle

import app.forku.domain.model.checklist.PreShiftCheck

data class Vehicle(
    val id: String,
    val codename: String,
    val type: VehicleType,
    val status: VehicleStatus = VehicleStatus.AVAILABLE,
    val photoUrl: String? = null,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val year: Int,
    val lastMaintenanceDate: String? = null,
    val description: String,
    val bestSuitedFor: String,
    val photoModel: String,
    val vehicleClass: String,
    val energyType: String,
    val nextService: String,
    val checks: List<PreShiftCheck>? = null
)