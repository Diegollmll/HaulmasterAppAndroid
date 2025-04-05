package app.forku.domain.usecase.vehicle

import app.forku.domain.model.vehicle.Vehicle
import javax.inject.Inject
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository

class GetVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String): Vehicle? {
        val currentUser = userRepository.getCurrentUser() ?: return null
        val businessId = currentUser.businessId ?: return null
        
        return try {
            repository.getVehicle(id, businessId)
        } catch (e: Exception) {
            android.util.Log.e("GetVehicleUseCase", "Error getting vehicle: ${e.message}")
            null
        }
    }
}