package app.forku.domain.usecase.vehicle

import app.forku.domain.model.vehicle.Vehicle
import javax.inject.Inject
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.core.Constants

class GetVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String): Vehicle? {
        val currentUser = userRepository.getCurrentUser() ?: return null
        // Use fallback business ID if user's businessId is null
        val businessId = currentUser.businessId ?: Constants.BUSINESS_ID
        android.util.Log.d("GetVehicleUseCase", "Fetching vehicle with id=$id, businessId=$businessId")
        return try {
            val vehicle = repository.getVehicle(id, businessId)
            android.util.Log.d("GetVehicleUseCase", "Fetched vehicle: $vehicle")
            vehicle
        } catch (e: Exception) {
            android.util.Log.e("GetVehicleUseCase", "Error getting vehicle: ${e.message}")
            null
        }
    }
}