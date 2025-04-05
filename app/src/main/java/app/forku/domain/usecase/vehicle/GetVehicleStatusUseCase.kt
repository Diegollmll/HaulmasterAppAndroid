package app.forku.domain.usecase.vehicle

import app.forku.domain.model.vehicle.VehicleStatus
import javax.inject.Inject
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.domain.repository.user.UserRepository

class GetVehicleStatusUseCase @Inject constructor(
    private val vehicleStatusRepository: VehicleStatusRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(vehicleId: String): VehicleStatus {
        return try {
            val currentUser = userRepository.getCurrentUser()
            val businessId = currentUser?.businessId
            
            if (businessId == null) {
                android.util.Log.e("VehicleStatus", "No business ID available")
                return VehicleStatus.AVAILABLE
            }
            
            vehicleStatusRepository.getVehicleStatus(vehicleId, businessId)
        } catch (e: Exception) {
            android.util.Log.e("VehicleStatus", "Error getting vehicle status", e)
            VehicleStatus.AVAILABLE
        }
    }
} 