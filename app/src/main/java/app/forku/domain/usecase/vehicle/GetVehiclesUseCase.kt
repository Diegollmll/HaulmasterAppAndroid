package app.forku.domain.usecase.vehicle

import app.forku.domain.model.vehicle.Vehicle
import javax.inject.Inject
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository

class GetVehiclesUseCase @Inject constructor(
    private val repository: VehicleRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<Vehicle> {
        val currentUser = userRepository.getCurrentUser() ?: return emptyList()
        val businessId = currentUser.businessId ?: return emptyList()
        
        return try {
            repository.getVehicles(businessId)
        } catch (e: Exception) {
            android.util.Log.e("GetVehiclesUseCase", "Error getting vehicles: ${e.message}")
            emptyList()
        }
    }
} 