package app.forku.domain.usecase.vehicle

import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.model.user.UserRole
import javax.inject.Inject

class GetActiveVehicleSessionsUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(): Map<String, VehicleSessionInfo> {
        val sessionMap = mutableMapOf<String, VehicleSessionInfo>()
        
        // Get all active sessions
        val activeSessions = sessionRepository.getOperatorSessionHistory()
            .filter { it.endTime == null }  // Only active sessions
            
        for (session in activeSessions) {
            val operator = userRepository.getUserById(session.userId)
            val vehicle = vehicleRepository.getVehicle(session.vehicleId)
            
            try {
                sessionMap[session.vehicleId] = VehicleSessionInfo(
                    vehicle = vehicle,
                    session = session,
                    operator = operator,
                    operatorName = operator?.let { "${it.firstName.first()}. ${it.lastName}" } ?: "Unknown",
                    operatorImage = operator?.photoUrl,
                    sessionStartTime = session.startTime,
                    userRole = operator?.role ?: UserRole.OPERATOR,
                    codename = vehicle.codename,
                    vehicleImage = vehicle.photoModel,
                    progress = 0f,
                    vehicleId = vehicle.id,
                    vehicleType = vehicle.type.displayName
                )
            } catch (e: Exception) {
                android.util.Log.e("GetActiveVehicleSessions", "Error getting session info for vehicle ${session.vehicleId}", e)
            }
        }
        
        return sessionMap
    }
} 