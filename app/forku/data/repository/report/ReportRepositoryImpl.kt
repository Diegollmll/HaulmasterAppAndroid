import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class ReportRepositoryImpl : ReportRepository {

    override suspend fun getVehiclesReport(filter: ReportFilter): List<VehicleReportItem> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Generating vehicles report with filter: ${filter.getFilterSummary()}")
            
            // Get current business context for security
            val currentBusinessId = businessContextManager.getCurrentBusinessId()
            Log.d(TAG, "🔒 Current business context: $currentBusinessId")
            
            // Apply business context filter automatically for security
            val secureFilter = filter.copy(businessId = currentBusinessId)
            Log.d(TAG, "🔒 Applied business context filter: ${secureFilter.getFilterSummary()}")
            
            val vehicles = vehicleRepository.getAllVehicles()
            Log.d(TAG, "🚗 Retrieved ${vehicles.size} vehicles from repository")
            
            vehicles.forEachIndexed { index, vehicle ->
                Log.d(TAG, "🚗 Vehicle $index: ${vehicle.model} (businessId: ${vehicle.businessId}, status: ${vehicle.status.name})")
            }
            
            val filteredVehicles = vehicles.mapNotNull { vehicle ->
                Log.d(TAG, "🔍 Processing vehicle: ${vehicle.model}")
                Log.d(TAG, "  - Vehicle businessId: ${vehicle.businessId}")
                Log.d(TAG, "  - Required businessId: ${secureFilter.businessId}")
                Log.d(TAG, "  - Vehicle siteId: ${vehicle.siteId}")
                Log.d(TAG, "  - Filter siteId: ${secureFilter.siteId}")
                Log.d(TAG, "  - Vehicle status: ${vehicle.status.name}")
                Log.d(TAG, "  - Filter status: ${secureFilter.status}")
                
                // SECURITY: Always apply business context filter first
                if (vehicle.businessId != currentBusinessId) {
                    Log.d(TAG, "  🔒 SECURITY: Filtered out - different business context")
                    return@mapNotNull null
                }
                
                // Apply additional filters
                if (secureFilter.siteId != null && vehicle.siteId != secureFilter.siteId) {
                    Log.d(TAG, "  ❌ Filtered out by siteId")
                    return@mapNotNull null
                }
                if (secureFilter.vehicleId != null && vehicle.id != secureFilter.vehicleId) {
                    Log.d(TAG, "  ❌ Filtered out by vehicleId")
                    return@mapNotNull null
                }
                if (secureFilter.status != null && vehicle.status.name != secureFilter.status) {
                    Log.d(TAG, "  ❌ Filtered out by status")
                    return@mapNotNull null
                }
                if (secureFilter.type != null && vehicle.type.Name != secureFilter.type) {
                    Log.d(TAG, "  ❌ Filtered out by type")
                    return@mapNotNull null
                }
                
                Log.d(TAG, "  ✅ Vehicle passed all filters")
                
                VehicleReportItem(
                    vehicleId = vehicle.id,
                    vehicleName = vehicle.model,
                    vehicleType = vehicle.type.Name,
                    status = vehicle.status.name,
                    lastSessionDate = null, // TODO: Get from session repository when available
                    lastSessionUser = null, // TODO: Get from session repository when available
                    totalSessions = 0, // TODO: Get from session repository when available
                    businessName = "Business", // TODO: Get from business repository
                    siteName = "Site" // TODO: Get from site repository
                )
            }
            
            Log.d(TAG, "🎯 Final result: ${filteredVehicles.size} vehicles after filtering")
            filteredVehicles
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating vehicles report", e)
            emptyList()
        }
    }
} 