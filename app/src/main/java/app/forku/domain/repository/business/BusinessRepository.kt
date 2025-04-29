package app.forku.domain.repository.business

import app.forku.data.api.dto.business.BusinessStats
import app.forku.domain.model.business.BusinessStatus
import app.forku.presentation.dashboard.Business
import app.forku.domain.model.user.User
import app.forku.data.api.dto.business.BusinessConfigurationDto

interface BusinessRepository {
    suspend fun getAllBusinesses(): List<Business>
    suspend fun getBusinessById(id: String): Business
    suspend fun createBusiness(name: String): Business
    suspend fun updateBusiness(business: Business): Business
    suspend fun deleteBusiness(id: String)
    
    // User-Business relationship methods
    suspend fun assignUserToBusiness(userId: String, businessId: String)
    suspend fun removeUserFromBusiness(userId: String, businessId: String)
    suspend fun getBusinessUsers(businessId: String): List<String>
    suspend fun getUserBusinesses(userId: String): List<Business>
    
    // Business management methods
    suspend fun getBusinessVehicles(businessId: String): List<String>
    suspend fun getBusinessStats(businessId: String): BusinessStats
    
    // Role-based access methods
    suspend fun validateUserAccess(userId: String, businessId: String): Boolean
    suspend fun validateAdminAccess(userId: String, businessId: String): Boolean
    suspend fun validateSuperAdminAccess(superAdminId: String, businessId: String?): Boolean
    suspend fun validateSystemOwnerAccess(systemOwnerId: String): Boolean
    suspend fun getBusinessesByRole(user: User): List<Business>
    suspend fun getBusinessesBySuperAdminId(superAdminId: String): List<Business>
    suspend fun transferBusinessToSuperAdmin(businessId: String, newSuperAdminId: String)
    
    // Business status management
    suspend fun updateBusinessStatus(businessId: String, newStatus: BusinessStatus)
} 