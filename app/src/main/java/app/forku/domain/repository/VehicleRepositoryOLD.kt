package app.forku.domain.repository

//interface VehicleRepository {
//    suspend fun getAllVehicles(): List<VehicleModel>
//    suspend fun getVehiclesByBusinessId(businessId: String): List<VehicleModel>
//    suspend fun getVehicleById(id: String): VehicleModel?
//    suspend fun createVehicle(vehicle: VehicleModel): VehicleModel
//    suspend fun updateVehicle(vehicle: VehicleModel): VehicleModel
//    suspend fun deleteVehicle(id: String)
//
//    suspend fun getAllVehicleTypes(): List<VehicleType>
//    suspend fun getVehicleTypeById(id: String): VehicleType?
//    suspend fun createVehicleType(vehicleType: VehicleType): VehicleType
//    suspend fun updateVehicleType(vehicleType: VehicleType): VehicleType
//    suspend fun deleteVehicleType(id: String)
//
//    suspend fun getAllVehicleCategories(): List<VehicleCategory>
//    suspend fun getVehicleCategoryById(id: String): VehicleCategory?
//    suspend fun createVehicleCategory(category: VehicleCategory): VehicleCategory
//    suspend fun updateVehicleCategory(category: VehicleCategory): VehicleCategory
//    suspend fun deleteVehicleCategory(id: String)
//
//    fun observeVehicles(): Flow<List<VehicleModel>>
//    fun observeVehicleTypes(): Flow<List<VehicleType>>
//    fun observeVehicleCategories(): Flow<List<VehicleCategory>>
//}