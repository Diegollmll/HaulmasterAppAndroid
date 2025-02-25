package app.forku.domain.model.vehicle

import app.forku.domain.model.checklist.PreShiftCheck

data class Vehicle(
    val id: String,
    val type: VehicleType,
    val status: String,
    val serialNumber: String,
    val description: String,
    val bestSuitedFor: String,
    val photoModel: String,
    val codename: String,
    val model: String,
    val vehicleClass: String,
    val energyType: String,
    val nextService: String,
    val qrCode: String,
    val checks: List<PreShiftCheck>? = null
)