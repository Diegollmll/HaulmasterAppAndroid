package app.forku.domain.repository.business

import app.forku.data.remote.dto.BusinessStats
import app.forku.presentation.dashboard.Business

interface BusinessRepository {
    suspend fun getAllBusinesses(): List<Business>
    suspend fun getBusinessById(id: String): Business
    suspend fun createBusiness(name: String): Business
    suspend fun updateBusiness(business: Business): Business
    suspend fun deleteBusiness(id: String)
    suspend fun assignUserToBusiness(userId: String, businessId: String)
    suspend fun removeUserFromBusiness(userId: String, businessId: String)
    suspend fun getBusinessUsers(businessId: String): List<String>
    suspend fun getBusinessVehicles(businessId: String): List<String>
    suspend fun getBusinessesBySystemOwnerId(systemOwnerId: String): List<Business>
    suspend fun getBusinessesBySuperAdminId(superAdminId: String): List<Business>
    suspend fun transferBusinessToSuperAdmin(businessId: String, newSuperAdminId: String)
    suspend fun validateSuperAdminAccess(superAdminId: String, businessId: String?): Boolean
    suspend fun getSystemOwnerBusinessStats(systemOwnerId: String): BusinessStats
    suspend fun getSuperAdminBusinessStats(superAdminId: String): BusinessStats
} 