package app.forku.data.repository.business

import android.util.Log
import app.forku.data.api.BusinessApi
import app.forku.data.api.UserBusinessApi
import app.forku.data.api.dto.business.BusinessItemDto
import app.forku.data.api.dto.business.UserBusinessAssignmentDto
import app.forku.data.api.dto.business.BusinessStats
import app.forku.domain.model.user.User
import app.forku.domain.model.user.UserRole
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.presentation.dashboard.Business
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject
import app.forku.data.api.CreateBusinessRequest
import app.forku.data.api.UpdateBusinessRequest
import app.forku.domain.model.business.BusinessStatus
import app.forku.data.api.BusinessConfigurationApi
import app.forku.data.api.dto.business.BusinessConfigurationDto


class BusinessRepositoryImpl @Inject constructor(
    private val api: BusinessApi,
    private val userBusinessApi: UserBusinessApi,
    private val userRepository: UserRepository,
    private val businessConfigurationApi: BusinessConfigurationApi
) : BusinessRepository {

    private suspend fun <T> executeApiCallForList(apiCall: suspend () -> Response<List<T>>): List<T> {
        return try {
            val response = apiCall()
            if (!response.isSuccessful) {
                Log.e("BusinessRepository", "API call failed with code: ${response.code()}")
                if (response.code() == 404) {
                    emptyList()
                } else {
                    throw HttpException(response)
                }
            } else {
                response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error executing API call", e)
            throw e
        }
    }

    private fun mapToDomain(businessDto: BusinessItemDto): Business {
        return try {
            Log.d("BusinessRepository", "Mapping DTO to Business")
            
            val businessStatus = when (businessDto.status) {
                0 -> BusinessStatus.PENDING
                1 -> BusinessStatus.ACTIVE
                2 -> BusinessStatus.SUSPENDED
                else -> BusinessStatus.PENDING
            }

            Business(
                id = businessDto.id,
                name = businessDto.name,
                totalUsers = 0,  // Will be updated separately
                totalVehicles = 0,  // Will be updated separately
                status = businessStatus,
                createdAt = businessDto.createdAt,
                updatedAt = businessDto.updatedAt,
                createdBy = businessDto.createdBy,
                updatedBy = businessDto.updatedBy,
                settings = businessDto.settings ?: emptyMap(),
                metadata = businessDto.metadata ?: emptyMap(),
                superAdminId = businessDto.superAdminId
            )
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error mapping DTO to Business", e)
            throw e
        }
    }

    override suspend fun getAllBusinesses(): List<Business> {
        return executeApiCallForList {
            Log.d("BusinessRepository", "Getting all businesses")
            api.getAllBusinesses()
        }.map { businessDto ->
            mapToDomain(businessDto)
        }
    }

    override suspend fun getBusinessById(id: String): Business {
        val response = api.getBusinessById(id)
        if (!response.isSuccessful) {
            throw Exception("Failed to get business: ${response.code()}")
        }
        return mapToDomain(response.body() ?: throw Exception("Business not found"))
    }

    override suspend fun createBusiness(name: String): Business {
        try {
            val currentUser = userRepository.getCurrentUser()
            when (currentUser?.role) {
                UserRole.SYSTEM_OWNER, UserRole.SUPERADMIN -> {
                    val businessDto = BusinessItemDto(
                        id = "",  // Will be assigned by server
                        name = name,
                        status = 0,  // PENDING status
                        businessConfigurationId = null,
                        countryId = null,
                        countryStateId = null,
                        isNew = true,
                        isDirty = true
                    )
                    
                    val response = api.saveBusiness(businessDto)
                    if (!response.isSuccessful) {
                        throw Exception("Failed to create business: ${response.code()}")
                    }
                    
                    val createdBusiness = mapToDomain(response.body() ?: throw Exception("Empty response body"))

                    // Assign the user to the business if they're a SuperAdmin
                    if (currentUser.role == UserRole.SUPERADMIN) {
                        assignUserToBusiness(currentUser.id, createdBusiness.id)
                    }

                    return createdBusiness
                }
                else -> throw SecurityException("Insufficient permissions to create business")
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error creating business", e)
            throw e
        }
    }

    override suspend fun getBusinessesBySuperAdminId(superAdminId: String): List<Business> {
        try {
            validateSuperAdminAccess(superAdminId, null)
            Log.d("BusinessRepository", "Getting businesses for super admin: $superAdminId")
            
            // Get all user-business assignments
            val assignments = userBusinessApi.getUserBusinessAssignments()
            if (!assignments.isSuccessful) {
                Log.e("BusinessRepository", "Failed to get user-business assignments: ${assignments.code()}")
                return emptyList()
            }
            
            // Filter assignments for this SuperAdmin
            val superAdminAssignments = assignments.body()?.filter { it.userId == superAdminId } ?: emptyList()
            
            // Get all businesses using executeApiCallForList
            return executeApiCallForList {
                api.getAllBusinesses()
            }.filter { businessDto ->
                // Only keep businesses that have an assignment for this SuperAdmin
                superAdminAssignments.any { it.businessId == businessDto.id }
            }.map { businessDto ->
                mapToDomain(businessDto)
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting businesses for SuperAdmin", e)
            return emptyList()
        }
    }

    override suspend fun transferBusinessToSuperAdmin(businessId: String, newSuperAdminId: String) {
        try {
            Log.d("BusinessManagement", "Attempting to transfer business $businessId to SuperAdmin $newSuperAdminId")
            val currentUser = userRepository.getCurrentUser()
            val business = getBusinessById(businessId)
            
            when {
                currentUser?.role == UserRole.SYSTEM_OWNER -> {
                    Log.d("BusinessManagement", "System Owner transferring business")
                    
                    // Get current assignments for this business
                    val assignments = userBusinessApi.getUserBusinessAssignments().body() ?: emptyList()
                    
                    // Find current SuperAdmin assignment if any
                    val currentSuperAdminAssignment = assignments.find { assignment ->
                        assignment.businessId == businessId && 
                        assignment.role == UserRole.SUPERADMIN.name
                    }
                    
                    // Remove current SuperAdmin if exists
                    if (currentSuperAdminAssignment != null) {
                        userBusinessApi.removeUserFromBusiness(businessId, currentSuperAdminAssignment.userId)
                        Log.d("BusinessManagement", "Removed previous SuperAdmin assignment")
                    }
                    
                    // If new SuperAdmin ID is not empty, create new assignment
                    if (newSuperAdminId.isNotEmpty()) {
                        val newAssignment = UserBusinessAssignmentDto(
                            businessId = businessId,
                            userId = newSuperAdminId,
                            role = UserRole.SUPERADMIN.name
                        )
                        userBusinessApi.assignUserToBusiness(newAssignment)
                        Log.d("BusinessManagement", "Created new SuperAdmin assignment")
                    }
                    
                    Log.d("BusinessManagement", "Business transfer completed successfully")
                }
                currentUser?.role == UserRole.SUPERADMIN -> {
                    // Get current assignments to verify ownership
                    val assignments = userBusinessApi.getUserBusinessAssignments().body() ?: emptyList()
                    val hasOwnership = assignments.any { 
                        it.businessId == businessId && 
                        it.userId == currentUser.id &&
                        it.role == UserRole.SUPERADMIN.name
                    }
                    
                    if (hasOwnership) {
                        Log.d("BusinessManagement", "Current SuperAdmin transferring their business")
                        
                        // Remove current SuperAdmin assignment
                        userBusinessApi.removeUserFromBusiness(businessId, currentUser.id)
                        
                        // If new SuperAdmin ID is not empty, create new assignment
                        if (newSuperAdminId.isNotEmpty()) {
                            val newAssignment = UserBusinessAssignmentDto(
                                businessId = businessId,
                                userId = newSuperAdminId,
                                role = UserRole.SUPERADMIN.name
                            )
                            userBusinessApi.assignUserToBusiness(newAssignment)
                            Log.d("BusinessManagement", "Created new SuperAdmin assignment")
                        }
                        
                        Log.d("BusinessManagement", "Business transfer completed successfully")
                    } else {
                        throw SecurityException("Current SuperAdmin does not have ownership of this business")
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
                        // Verificar si el SuperAdmin tiene asignación para este negocio
                        val assignments = userBusinessApi.getUserBusinessAssignments().body() ?: emptyList()
                        assignments.any { assignment ->
                            assignment.userId == superAdminId &&
                            assignment.businessId == businessId &&
                            assignment.role == UserRole.SUPERADMIN.name
                        }
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

    override suspend fun validateSystemOwnerAccess(systemOwnerId: String): Boolean {
        try {
            val currentUser = userRepository.getCurrentUser()
            Log.d("BusinessManagement", "Validating SystemOwner access for user: ${currentUser?.id}, role: ${currentUser?.role}")
            
            val hasAccess = currentUser?.role == UserRole.SYSTEM_OWNER && currentUser.id == systemOwnerId
            
            if (!hasAccess) {
                Log.w("BusinessManagement", "Access denied for user ${currentUser?.id} to SystemOwner resources")
            }
            
            return hasAccess
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error validating SystemOwner access", e)
            return false
        }
    }

    override suspend fun updateBusiness(business: Business): Business {
        try {
            val currentUser = userRepository.getCurrentUser()
            if (!validateAdminAccess(currentUser?.id ?: "", business.id)) {
                throw SecurityException("Insufficient permissions to update business")
            }

            val businessDto = BusinessItemDto(
                id = business.id,
                name = business.name,
                status = when(business.status) {
                    BusinessStatus.ACTIVE -> 1
                    BusinessStatus.PENDING -> 0
                    BusinessStatus.SUSPENDED -> 2
                },
                businessConfigurationId = null,
                countryId = null,
                countryStateId = null,
                isDirty = true
            )

            // Usar saveBusiness para crear o actualizar
            val response = api.saveBusiness(businessDto)
            if (!response.isSuccessful) {
                throw Exception("Failed to update business: ${response.code()}")
            }
            
            return mapToDomain(response.body() ?: throw Exception("Empty response body"))
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error updating business", e)
            throw e
        }
    }

    override suspend fun deleteBusiness(id: String) {
        try {
            val currentUser = userRepository.getCurrentUser()
            if (!validateAdminAccess(currentUser?.id ?: "", id)) {
                throw SecurityException("Insufficient permissions to delete business")
            }

            val response = api.deleteBusiness(id)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete business: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error deleting business", e)
            throw e
        }
    }

    override suspend fun assignUserToBusiness(userId: String, businessId: String) {
        try {
            val assignment = UserBusinessAssignmentDto(
                businessId = businessId,
                userId = userId
            )
            val response = userBusinessApi.assignUserToBusiness(assignment)
            if (!response.isSuccessful) {
                throw Exception("Failed to assign user to business: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error assigning user to business", e)
            throw e
        }
    }

    override suspend fun removeUserFromBusiness(userId: String, businessId: String) {
        try {
            val response = userBusinessApi.removeUserFromBusiness(businessId, userId)
            if (!response.isSuccessful) {
                throw Exception("Failed to remove user from business: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error removing user from business", e)
            throw e
        }
    }

    override suspend fun getBusinessUsers(businessId: String): List<String> {
        return try {
            val response = api.getBusinessUsers(businessId)
            if (!response.isSuccessful) {
                emptyList()
            } else {
                response.body()?.mapNotNull { it.id } ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting business users", e)
            emptyList()
        }
    }

    override suspend fun getBusinessVehicles(businessId: String): List<String> {
        return try {
            val response = api.getBusinessVehicles(businessId)
            if (!response.isSuccessful) {
                emptyList()
            } else {
                response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting business vehicles", e)
            emptyList()
        }
    }

    override suspend fun getBusinessesByRole(user: User): List<Business> {
        return try {
            Log.d("BusinessRepository", "Getting businesses for user ${user.id} with role ${user.role}")
            
            when (user.role) {
                UserRole.SYSTEM_OWNER -> {
                    // System Owner can see all businesses
                    executeApiCallForList {
                        api.getAllBusinesses()
                    }.map { businessDto ->
                        mapToDomain(businessDto)
                    }
                }
                UserRole.SUPERADMIN, UserRole.ADMIN -> {
                    // Get businesses through user-business assignments
                    val assignments = userBusinessApi.getUserBusinessAssignments().body() ?: emptyList()
                    val userAssignments = assignments.filter { it.userId == user.id }
                    
                    userAssignments.mapNotNull { assignment ->
                        try {
                            val business = getBusinessById(assignment.businessId)
                            business
                        } catch (e: Exception) {
                            Log.e("BusinessRepository", "Error getting business ${assignment.businessId}", e)
                            null
                        }
                    }
                }
                else -> {
                    Log.w("BusinessRepository", "Unauthorized role ${user.role}, returning empty list")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting businesses by role", e)
            throw e
        }
    }

    override suspend fun validateUserAccess(userId: String, businessId: String): Boolean {
        return try {
            val response = userBusinessApi.getUserBusinessAssignment(businessId, userId)
            response.isSuccessful && response.body() != null
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error validating user access", e)
            false
        }
    }

    override suspend fun validateAdminAccess(userId: String, businessId: String): Boolean {
        val user = userRepository.getUserById(userId) ?: return false
        return when (user.role) {
            UserRole.SYSTEM_OWNER -> true
            UserRole.SUPERADMIN, UserRole.ADMIN -> validateUserAccess(userId, businessId)
            else -> false
        }
    }

    override suspend fun getUserBusinesses(userId: String): List<Business> {
        return try {
            val assignments = userBusinessApi.getUserBusinessAssignments().body() ?: emptyList()
            val userAssignments = assignments.filter { it.userId == userId }
            
            userAssignments.mapNotNull { assignment ->
                try {
                    getBusinessById(assignment.businessId)
                } catch (e: Exception) {
                    Log.e("BusinessRepository", "Error getting business ${assignment.businessId}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting user businesses", e)
            emptyList()
        }
    }

    override suspend fun updateBusinessStatus(businessId: String, newStatus: BusinessStatus) {
        val business = getBusinessById(businessId)
        val updatedBusiness = business.copy(status = newStatus)
        updateBusiness(updatedBusiness)
    }

    override suspend fun getBusinessStats(businessId: String): BusinessStats {
        try {
            Log.d("BusinessRepository", "Getting stats for business: $businessId")
            
            // Get business details
            val business = getBusinessById(businessId)
            
            // Get users count
            val users = executeApiCallForList {
                api.getBusinessUsers(businessId)
            }
            
            // Get vehicles count
            val vehicles = executeApiCallForList {
                api.getBusinessVehicles(businessId)
            }
            
            // Get user assignments to understand roles distribution
            val assignments = userBusinessApi.getUserBusinessAssignments().body() ?: emptyList()
            val businessAssignments = assignments.filter { it.businessId == businessId }
            
            // Count by role
            val adminCount = businessAssignments.count { it.role == UserRole.ADMIN.name }
            val operatorCount = businessAssignments.count { it.role == UserRole.OPERATOR.name }
            
            return BusinessStats(
                totalUsers = users.size,
                totalVehicles = vehicles.size,
                activeUsers = users.size, // Could be refined if we track active status
                activeVehicles = vehicles.size, // Could be refined if we track active status
                adminCount = adminCount,
                operatorCount = operatorCount,
                businessStatus = business.status,
                lastUpdated = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            Log.e("BusinessRepository", "Error getting business stats", e)
            throw Exception("Failed to get business stats: ${e.message}")
        }
    }
} 