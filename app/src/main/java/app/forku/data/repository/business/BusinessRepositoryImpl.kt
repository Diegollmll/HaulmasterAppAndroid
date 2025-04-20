package app.forku.data.repository.business

import app.forku.data.api.BusinessApi
import app.forku.data.api.CreateBusinessRequest
import app.forku.domain.repository.business.BusinessRepository
import app.forku.presentation.dashboard.Business
import app.forku.presentation.dashboard.BusinessStatus
import retrofit2.HttpException
import javax.inject.Inject
import android.util.Log
import app.forku.data.api.dto.BusinessDto
import app.forku.data.api.dto.BusinessStats
import com.google.gson.Gson
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.user.UserRepository
import app.forku.data.api.UpdateBusinessRequest

class BusinessRepositoryImpl @Inject constructor(
    private val api: BusinessApi,
    private val gson: Gson,
    private val userRepository: UserRepository
) : BusinessRepository {

    override suspend fun getAllBusinesses(): List<Business> {
        try {
            val currentUser = userRepository.getCurrentUser()
            Log.d("BusinessManagement", "Fetching businesses for user role: ${currentUser?.role}, userId: ${currentUser?.id}")
            
            val businessDtos = when (currentUser?.role) {
                UserRole.SYSTEM_OWNER -> {
                    Log.d("BusinessManagement", "Fetching all businesses as SYSTEM_OWNER")
                    try {
                        api.getAllBusinesses()
                    } catch (e: HttpException) {
                        Log.e("BusinessManagement", "HTTP error fetching businesses: ${e.code()}", e)
                        if (e.code() == 404) {
                            Log.d("BusinessManagement", "No businesses found, returning empty list")
                            emptyList()
                        } else {
                            throw e
                        }
                    }
                }
                UserRole.SUPERADMIN -> {
                    Log.d("BusinessManagement", "Fetching businesses for SUPERADMIN: ${currentUser.id}")
                    try {
                        api.getAllBusinesses(superAdminId = currentUser.id)
                    } catch (e: HttpException) {
                        Log.e("BusinessManagement", "HTTP error fetching businesses: ${e.code()}", e)
                        if (e.code() == 404) {
                            Log.d("BusinessManagement", "No businesses found, returning empty list")
                            emptyList()
                        } else {
                            throw e
                        }
                    }
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
            return emptyList()
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
        try {
            Log.d("BusinessManagement", "Attempting to transfer business $businessId to SuperAdmin $newSuperAdminId")
            val currentUser = userRepository.getCurrentUser()
            val business = getBusinessById(businessId)
            
            when {
                currentUser?.role == UserRole.SYSTEM_OWNER -> {
                    Log.d("BusinessManagement", "System Owner transferring business")
                    val request = UpdateBusinessRequest(
                        name = business.name,
                        status = business.status.name.uppercase(),
                        superAdminId = newSuperAdminId.ifEmpty { "" }
                    )
                    try {
                        val updatedBusiness = api.updateBusiness(businessId, request)
                        Log.d("BusinessManagement", "Business transferred successfully. New SuperAdmin: ${updatedBusiness.superAdminId}")
                    } catch (e: Exception) {
                        Log.e("BusinessManagement", "Failed to transfer business", e)
                        throw Exception("Failed to transfer business: ${e.message}")
                    }
                }
                currentUser?.role == UserRole.SUPERADMIN && business.superAdminId == currentUser.id -> {
                    Log.d("BusinessManagement", "Current SuperAdmin transferring their business")
                    val request = UpdateBusinessRequest(
                        name = business.name,
                        status = business.status.name.uppercase(),
                        superAdminId = newSuperAdminId.ifEmpty { "" }
                    )
                    try {
                        val updatedBusiness = api.updateBusiness(businessId, request)
                        Log.d("BusinessManagement", "Business transferred successfully. New SuperAdmin: ${updatedBusiness.superAdminId}")
                    } catch (e: Exception) {
                        Log.e("BusinessManagement", "Failed to transfer business", e)
                        throw Exception("Failed to transfer business: ${e.message}")
                    }
                }
                else -> {
                    Log.e("BusinessManagement", "Unauthorized attempt to transfer business")
                    throw SecurityException("Insufficient permissions to transfer business")
                }
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error transferring business", e)
            throw e
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

    private suspend fun mapDtoToBusiness(dto: BusinessDto): Business {
        return try {
            Log.d("BusinessManagement", "Starting to map DTO to Business")
            Log.d("BusinessManagement", "Raw DTO data: " +
                "id=${dto.id}, " +
                "name=${dto.name}, " +
                "status=${dto.status}, " +
                "systemOwnerId=${dto.systemOwnerId}, " +
                "superAdminId=${dto.superAdminId}, " +
                "totalUsers=${dto.totalUsers}, " +
                "totalVehicles=${dto.totalVehicles}")
                
            // No need to uppercase since the status is already in uppercase from the API
            val businessStatus = try {
                BusinessStatus.valueOf(dto.status)
            } catch (e: IllegalArgumentException) {
                Log.w("BusinessManagement", "Invalid status value: ${dto.status}, attempting to normalize")
                BusinessStatus.valueOf(dto.status.trim())
            }
            
            Log.d("BusinessManagement", "Parsed business status: $businessStatus")

            // Get real count of users for this business
            val realUserCount = getBusinessUsers(dto.id).size
            Log.d("BusinessManagement", "Real user count for business ${dto.id}: $realUserCount")
                
            Business(
                id = dto.id,
                name = dto.name,
                totalUsers = realUserCount,
                totalVehicles = dto.totalVehicles,
                status = businessStatus,
                systemOwnerId = dto.systemOwnerId,
                superAdminId = dto.superAdminId,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            ).also { business ->
                Log.d("BusinessManagement", "Successfully mapped Business: " +
                    "id=${business.id}, " +
                    "name=${business.name}, " +
                    "status=${business.status}, " +
                    "systemOwnerId=${business.systemOwnerId}, " +
                    "superAdminId=${business.superAdminId}, " +
                    "totalUsers=${business.totalUsers}, " +
                    "totalVehicles=${business.totalVehicles}")
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error mapping DTO to Business: ${dto.id}", e)
            Log.e("BusinessManagement", "Failed DTO data: $dto")
            throw Exception("Failed to map business data: ${e.message}")
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
        try {
            val currentUser = userRepository.getCurrentUser()
            Log.d("BusinessManagement", "Attempting to update business: ${business.id}")
            Log.d("BusinessManagement", "Update details: name=${business.name}, status=${business.status}, superAdminId=${business.superAdminId}")
            Log.d("BusinessManagement", "Current user role: ${currentUser?.role}")

            when (currentUser?.role) {
                UserRole.SYSTEM_OWNER -> {
                    Log.d("BusinessManagement", "System Owner updating business")
                }
                UserRole.SUPERADMIN -> {
                    if (!validateSuperAdminAccess(currentUser.id, business.id)) {
                        Log.e("BusinessManagement", "SuperAdmin attempted to update unauthorized business")
                        throw SecurityException("Insufficient permissions to update this business")
                    }
                    Log.d("BusinessManagement", "SuperAdmin updating their business")
                }
                else -> {
                    Log.e("BusinessManagement", "Unauthorized role attempted to update business: ${currentUser?.role}")
                    throw SecurityException("Insufficient permissions to update business")
                }
            }

            // Create and log the request
            val request = UpdateBusinessRequest(
                name = business.name,
                status = business.status.name.uppercase(),
                superAdminId = business.superAdminId
            )
            
            Log.d("BusinessManagement", "Sending API request: PUT /business/${business.id}")
            Log.d("BusinessManagement", "Request body: $request")

            val dto = api.updateBusiness(
                id = business.id,
                request = request
            )
            
            Log.d("BusinessManagement", "API response received")
            Log.d("BusinessManagement", "Response DTO: id=${dto.id}, name=${dto.name}, status=${dto.status}, superAdminId=${dto.superAdminId}")

            return mapDtoToBusiness(dto).also { mappedBusiness ->
                Log.d("BusinessManagement", "Final mapped business: " +
                    "id=${mappedBusiness.id}, " +
                    "name=${mappedBusiness.name}, " +
                    "status=${mappedBusiness.status}, " +
                    "systemOwnerId=${mappedBusiness.systemOwnerId}, " +
                    "superAdminId=${mappedBusiness.superAdminId}")
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error updating business", e)
            Log.e("BusinessManagement", "Error details: ${e.message}")
            if (e is HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("BusinessManagement", "HTTP error response: $errorBody")
            }
            handleException(e, "update business")
            throw e
        }
    }

    override suspend fun deleteBusiness(id: String) {
        api.deleteBusiness(id)
    }

    override suspend fun assignUserToBusiness(userId: String, businessId: String) {
        try {
            Log.d("BusinessRepository", "Assigning user $userId to business $businessId")
            
            // Get the user first
            val user = userRepository.getUserById(userId) ?: throw Exception("User not found")
            
            // Update the user's businessId
            val updatedUser = user.copy(businessId = businessId)
            userRepository.updateUser(updatedUser)
            
            Log.d("BusinessRepository", "Successfully assigned user to business")
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error assigning user to business", e)
            throw e
        }
    }

    override suspend fun removeUserFromBusiness(userId: String, businessId: String) {
        try {
            Log.d("BusinessRepository", "Removing user $userId from business $businessId")
            
            // Get the user first
            val user = userRepository.getUserById(userId) ?: throw Exception("User not found")
            
            // Only remove if the user belongs to this business
            if (user.businessId == businessId) {
                val updatedUser = user.copy(businessId = null)
                userRepository.updateUser(updatedUser)
                Log.d("BusinessRepository", "Successfully removed user from business")
            } else {
                Log.w("BusinessRepository", "User does not belong to this business")
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error removing user from business", e)
            throw e
        }
    }

    override suspend fun getBusinessUsers(businessId: String): List<String> {
        return try {
            Log.d("BusinessRepository", "Getting users for business: $businessId")
            val users = api.getBusinessUsers(businessId).map { it.id }
            Log.d("BusinessRepository", "Found ${users.size} users for business $businessId")
            users
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting business users", e)
            emptyList()
        }
    }

    override suspend fun getBusinessVehicles(businessId: String): List<String> {
        return api.getBusinessVehicles(businessId)
    }
} 