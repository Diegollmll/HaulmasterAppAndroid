package app.forku.domain.repository.business

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
} 