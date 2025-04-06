package app.forku.data.repository.business

import app.forku.data.remote.api.BusinessApi
import app.forku.data.remote.api.CreateBusinessRequest
import app.forku.domain.repository.business.BusinessRepository
import app.forku.presentation.dashboard.Business
import app.forku.presentation.dashboard.BusinessStatus
import retrofit2.HttpException
import javax.inject.Inject
import android.util.Log
import app.forku.data.remote.dto.BusinessDto
import app.forku.data.remote.dto.BusinessStats
import com.google.gson.Gson
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.user.UserRepository

class BusinessRepositoryImpl @Inject constructor(
    private val api: BusinessApi,
    private val gson: Gson,
    private val userRepository: UserRepository
) : BusinessRepository {

    override suspend fun getAllBusinesses(): List<Business> {
        try {
            val currentUser = userRepository.getCurrentUser()
            Log.d("BusinessManagement", "Fetching businesses for user role: ${currentUser?.role}")
            
            val businessDtos = when (currentUser?.role) {
                UserRole.SYSTEM_OWNER -> {
                    Log.d("BusinessManagement", "Fetching all businesses as SYSTEM_OWNER")
                    api.getAllBusinesses()
                }
                UserRole.SUPERADMIN -> {
                    Log.d("BusinessManagement", "Fetching businesses for SUPERADMIN: ${currentUser.id}")
                    api.getBusinessesBySuperAdminId(currentUser.id)
                }
                else -> {
                    Log.e("BusinessManagement", "Unauthorized role: ${currentUser?.role}")
                    throw SecurityException("Insufficient permissions to access businesses")
                }
            }
            
            Log.d("BusinessManagement", "Received ${businessDtos.size} businesses from API")
            businessDtos.forEach { dto ->
                Log.d("BusinessManagement", "Business DTO: id=${dto.id}, name=${dto.name}, status=${dto.status}")
            }
            
            return businessDtos.map { dto ->
                mapDtoToBusiness(dto).also { business ->
                    Log.d("BusinessManagement", "Mapped business: id=${business.id}, name=${business.name}, status=${business.status}")
                }
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error fetching businesses", e)
            throw e
        }
    }

    override suspend fun getBusinessById(id: String): Business {
        val dto = api.getBusinessById(id)
        return mapDtoToBusiness(dto)
    }

    override suspend fun createBusiness(name: String): Business {
        try {
            val currentUser = userRepository.getCurrentUser()
            when (currentUser?.role) {
                UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> {
                    val request = CreateBusinessRequest(
                        name = name,
                        systemOwnerId = if (currentUser.role == UserRole.SYSTEM_OWNER) currentUser.id else null,
                        superAdminId = if (currentUser.role == UserRole.SUPERADMIN) currentUser.id else null
                    )
                    
                    val response = api.createBusiness(request)
                    if (!response.isSuccessful) {
                        handleErrorResponse(response)
                    }
                    
                    val dto = response.body() ?: throw Exception("Empty response body")
                    val business = mapDtoToBusiness(dto)

                    // Update user's systemOwnerId if they are a SYSTEM_OWNER
                    if (currentUser.role == UserRole.SYSTEM_OWNER) {
                        val updatedUser = currentUser.copy(
                            systemOwnerId = currentUser.id,
                            businessId = business.id
                        )
                        userRepository.updateUser(updatedUser)
                    }

                    return business
                }
                else -> throw SecurityException("Insufficient permissions to create business")
            }
        } catch (e: Exception) {
            handleException(e, "create business")
            throw e
        }
    }

    override suspend fun getBusinessesBySystemOwnerId(systemOwnerId: String): List<Business> {
        validateSystemOwnerAccess(systemOwnerId)
        return api.getBusinessesBySystemOwnerId(systemOwnerId).map { mapDtoToBusiness(it) }
    }

    override suspend fun getBusinessesBySuperAdminId(superAdminId: String): List<Business> {
        validateSuperAdminAccess(superAdminId, null)
        return api.getBusinessesBySuperAdminId(superAdminId).map { mapDtoToBusiness(it) }
    }

    override suspend fun transferBusinessToSuperAdmin(businessId: String, newSuperAdminId: String) {
        val currentUser = userRepository.getCurrentUser()
        val business = getBusinessById(businessId)
        
        when {
            currentUser?.role == UserRole.SYSTEM_OWNER -> {
                api.transferBusiness(businessId, newSuperAdminId)
            }
            currentUser?.role == UserRole.SUPERADMIN && business.superAdminId == currentUser.id -> {
                api.transferBusiness(businessId, newSuperAdminId)
            }
            else -> throw SecurityException("Insufficient permissions to transfer business")
        }
    }

    override suspend fun validateSuperAdminAccess(superAdminId: String, businessId: String?): Boolean {
        try {
            val currentUser = userRepository.getCurrentUser()
            Log.d("BusinessManagement", "Validating SuperAdmin access for user: ${currentUser?.id}, role: ${currentUser?.role}")
            
            return when {
                // System Owner tiene acceso total
                currentUser?.role == UserRole.SYSTEM_OWNER -> true
                
                // SuperAdmin solo puede acceder a sus propios negocios
                currentUser?.role == UserRole.SUPERADMIN && currentUser.id == superAdminId -> {
                    if (businessId == null) {
                        true // SuperAdmin can access their own general resources without a specific business
                    } else {
                        val business = getBusinessById(businessId)
                        business.superAdminId == superAdminId
                    }
                }
                
                // Otros roles no tienen acceso
                else -> false
            }.also {
                Log.d("BusinessManagement", "Access validation result: $it")
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error validating SuperAdmin access", e)
            return false
        }
    }

    override suspend fun getSystemOwnerBusinessStats(systemOwnerId: String): BusinessStats {
        validateSystemOwnerAccess(systemOwnerId)
        return api.getSystemOwnerBusinessStats(systemOwnerId)
    }

    override suspend fun getSuperAdminBusinessStats(superAdminId: String): BusinessStats {
        validateSuperAdminAccess(superAdminId, null)
        return api.getSuperAdminBusinessStats(superAdminId)
    }

    private suspend fun validateSystemOwnerAccess(systemOwnerId: String) {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser?.role != UserRole.SYSTEM_OWNER || currentUser.id != systemOwnerId) {
            throw SecurityException("Insufficient permissions to access system owner resources")
        }
    }

    private fun mapDtoToBusiness(dto: BusinessDto): Business {
        return try {
            Log.d("BusinessManagement", "Mapping DTO to Business: " +
                "id=${dto.id}, " +
                "name=${dto.name}, " +
                "status=${dto.status}, " +
                "systemOwnerId=${dto.systemOwnerId}, " +
                "superAdminId=${dto.superAdminId}")
                
            Business(
                id = dto.id,
                name = dto.name,
                totalUsers = dto.totalUsers,
                totalVehicles = dto.totalVehicles,
                status = BusinessStatus.valueOf(dto.status.uppercase()),
                systemOwnerId = dto.systemOwnerId,
                superAdminId = dto.superAdminId,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            ).also {
                Log.d("BusinessManagement", "Successfully mapped Business: " +
                    "id=${it.id}, " +
                    "name=${it.name}, " +
                    "status=${it.status}, " +
                    "systemOwnerId=${it.systemOwnerId}, " +
                    "superAdminId=${it.superAdminId}")
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error mapping DTO to Business: ${dto.id}", e)
            throw e
        }
    }

    private fun handleErrorResponse(response: retrofit2.Response<BusinessDto>) {
        val errorBody = response.errorBody()?.string()
        Log.e("BusinessManagement", "Error response: ${response.code()} - $errorBody")
        Log.e("BusinessManagement", "Request URL: ${response.raw().request.url}")
        Log.e("BusinessManagement", "Request headers: ${response.raw().request.headers}")
        throw Exception("Failed to create business: [${response.code()}] $errorBody")
    }

    private fun handleException(e: Exception, operation: String) {
        when (e) {
            is HttpException -> {
                val errorBody = e.response()?.errorBody()?.string()
                val errorCode = e.response()?.code()
                Log.e("BusinessManagement", "HTTP error: $errorCode - $errorBody")
                Log.e("BusinessManagement", "Request URL: ${e.response()?.raw()?.request?.url}")
                Log.e("BusinessManagement", "Request headers: ${e.response()?.raw()?.request?.headers}")
                throw Exception("Failed to $operation: [$errorCode] $errorBody")
            }
            else -> {
                Log.e("BusinessManagement", "Unexpected error", e)
                throw Exception("Failed to $operation: ${e.message}")
            }
        }
    }

    override suspend fun updateBusiness(business: Business): Business {
        val dto = api.updateBusiness(
            id = business.id,
            name = business.name,
            status = business.status.name.lowercase()
        )
        return mapDtoToBusiness(dto)
    }

    override suspend fun deleteBusiness(id: String) {
        api.deleteBusiness(id)
    }

    override suspend fun assignUserToBusiness(userId: String, businessId: String) {
        api.assignUserToBusiness(userId, businessId)
    }

    override suspend fun removeUserFromBusiness(userId: String, businessId: String) {
        api.removeUserFromBusiness(userId, businessId)
    }

    override suspend fun getBusinessUsers(businessId: String): List<String> {
        return api.getBusinessUsers(businessId)
    }

    override suspend fun getBusinessVehicles(businessId: String): List<String> {
        return api.getBusinessVehicles(businessId)
    }
} 