package app.forku.domain.usecase.vehicle

import app.forku.domain.model.session.VehicleSessionInfo
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class GetVehicleActiveSessionUseCase @Inject constructor(
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) {
    suspend operator fun invoke(vehicleId: String): VehicleSessionInfo? {
        // Get current user's business ID
        val currentUser = userRepository.getCurrentUser() ?: return null
        val businessId = currentUser.businessId ?: return null
        
        val session = vehicleSessionRepository.getActiveSessionForVehicle(vehicleId, businessId) ?: return null
        val operator = userRepository.getUserById(session.userId)
        val vehicle = vehicleRepository.getVehicle(vehicleId, businessId)
        
        val startTime = LocalDateTime.parse(session.startTime)
        val currentTime = LocalDateTime.now()
        val elapsedMinutes = ChronoUnit.MINUTES.between(startTime, currentTime)
        val sessionDuration = session.durationMinutes ?: 480 // Default to 8 hours if null
        val progress = (elapsedMinutes.toFloat() / sessionDuration.toFloat()).coerceIn(0f, 1f)
        
        return VehicleSessionInfo(
            vehicle = vehicle,
            session = session,
            operator = operator,
            operatorName = "${operator?.firstName?.firstOrNull() ?: ""}.${operator?.lastName ?: ""}",
            operatorImage = operator?.photoUrl,
            sessionStartTime = session.startTime,
            userRole = operator?.role ?: UserRole.OPERATOR,
            codename = vehicle.codename,
            vehicleImage = vehicle.photoModel,
            progress = progress,
            vehicleId = vehicleId,
            vehicleType = vehicle.type.displayName
        )
    }
} 