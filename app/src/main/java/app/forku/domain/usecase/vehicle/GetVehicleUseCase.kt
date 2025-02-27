package app.forku.domain.usecase.vehicle

import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.repository.vehicle.VehicleRepository
import javax.inject.Inject

class GetVehicleUseCase @Inject constructor(
    private val repository: VehicleRepository
) {
    suspend operator fun invoke(id: String): Vehicle {
        return repository.getVehicle(id)
    }
}