package app.forku.domain.repository.session

import app.forku.domain.model.session.VehicleSession

interface SessionStatusChecker {
    suspend fun getActiveSessionForVehicle(vehicleId: String): VehicleSession?
} 