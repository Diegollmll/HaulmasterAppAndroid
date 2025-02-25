package app.forku.data.api.dto.vehicle

import app.forku.data.api.dto.checklist.PreShiftCheckDto

data class VehicleDto(
    val id: String,
    val type: VehicleTypeDto,
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
    val checks: List<PreShiftCheckDto>? = null
)
