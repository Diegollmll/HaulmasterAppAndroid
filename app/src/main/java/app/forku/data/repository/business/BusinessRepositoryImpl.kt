package app.forku.data.repository.business

import app.forku.data.remote.api.BusinessApi
import app.forku.data.remote.api.CreateBusinessRequest
import app.forku.domain.repository.business.BusinessRepository
import app.forku.presentation.dashboard.Business
import app.forku.presentation.dashboard.BusinessStatus
import retrofit2.HttpException
import javax.inject.Inject
import android.util.Log
import com.google.gson.Gson

class BusinessRepositoryImpl @Inject constructor(
    private val api: BusinessApi,
    private val gson: Gson
) : BusinessRepository {

    override suspend fun getAllBusinesses(): List<Business> {
        try {
            Log.d("BusinessManagement", "Fetching all businesses")
            val businesses = api.getAllBusinesses()
            Log.d("BusinessManagement", "Received ${businesses.size} businesses")
            return businesses.map { dto ->
                Log.d("BusinessManagement", "Mapping business: ${dto.name}")
                Business(
                    id = dto.id,
                    name = dto.name,
                    totalUsers = dto.totalUsers,
                    totalVehicles = dto.totalVehicles,
                    status = BusinessStatus.valueOf(dto.status.uppercase())
                )
            }
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Error fetching businesses", e)
            throw e
        }
    }

    override suspend fun getBusinessById(id: String): Business {
        val dto = api.getBusinessById(id)
        return Business(
            id = dto.id,
            name = dto.name,
            totalUsers = dto.totalUsers,
            totalVehicles = dto.totalVehicles,
            status = BusinessStatus.valueOf(dto.status.uppercase())
        )
    }

    override suspend fun createBusiness(name: String): Business {
        try {
            val request = CreateBusinessRequest(name)
            val jsonRequest = gson.toJson(request)
            Log.d("BusinessManagement", "Sending request JSON: $jsonRequest")
            Log.d("BusinessManagement", "Request object: $request")
            
            val response = api.createBusiness(request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("BusinessManagement", "Error response: ${response.code()} - $errorBody")
                Log.e("BusinessManagement", "Request URL: ${response.raw().request.url}")
                Log.e("BusinessManagement", "Request headers: ${response.raw().request.headers}")
                throw Exception("Failed to create business: [${response.code()}] $errorBody")
            }
            
            val dto = response.body() ?: throw Exception("Empty response body")
            return Business(
                id = dto.id,
                name = dto.name,
                totalUsers = dto.totalUsers,
                totalVehicles = dto.totalVehicles,
                status = BusinessStatus.valueOf(dto.status.uppercase())
            )
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorCode = e.response()?.code()
            Log.e("BusinessManagement", "HTTP error: $errorCode - $errorBody")
            Log.e("BusinessManagement", "Request URL: ${e.response()?.raw()?.request?.url}")
            Log.e("BusinessManagement", "Request headers: ${e.response()?.raw()?.request?.headers}")
            throw Exception("Failed to create business: [$errorCode] $errorBody")
        } catch (e: Exception) {
            Log.e("BusinessManagement", "Unexpected error", e)
            throw Exception("Failed to create business: ${e.message}")
        }
    }

    override suspend fun updateBusiness(business: Business): Business {
        val dto = api.updateBusiness(
            id = business.id,
            name = business.name,
            status = business.status.name.lowercase()
        )
        return Business(
            id = dto.id,
            name = dto.name,
            totalUsers = dto.totalUsers,
            totalVehicles = dto.totalVehicles,
            status = BusinessStatus.valueOf(dto.status.uppercase())
        )
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