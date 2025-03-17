package app.forku.data.manager

import app.forku.data.model.VehicleSessionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleSessionManager @Inject constructor() {
    private val sessionMap = MutableStateFlow<Map<String, VehicleSessionDto>>(emptyMap())

    fun observeSession(vehicleId: String): Flow<VehicleSessionDto?> {
        return sessionMap.map { it[vehicleId] }
    }

    fun updateSession(vehicleId: String, session: VehicleSessionDto?) {
        sessionMap.value = if (session == null) {
            sessionMap.value - vehicleId
        } else {
            sessionMap.value + (vehicleId to session)
        }
    }

    fun clearSessions() {
        sessionMap.value = emptyMap()
    }
} 