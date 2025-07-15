package app.forku.domain.usecase.certification

import app.forku.domain.model.vehicle.VehicleType
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import javax.inject.Inject

class GetVehicleTypesUseCase @Inject constructor(
    private val vehicleTypeRepository: VehicleTypeRepository
) {
    suspend operator fun invoke(): List<VehicleType> =
        vehicleTypeRepository.getVehicleTypes()
} 