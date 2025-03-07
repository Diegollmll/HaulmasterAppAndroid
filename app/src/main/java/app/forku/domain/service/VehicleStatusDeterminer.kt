package app.forku.domain.service

import app.forku.domain.model.vehicle.VehicleStatus

interface VehicleStatusDeterminer {
    fun determineStatusFromCheck(checkStatus: String): VehicleStatus
} 