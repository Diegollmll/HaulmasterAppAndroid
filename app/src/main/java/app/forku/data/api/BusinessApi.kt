package app.forku.data.api

import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.BusinessDto
import app.forku.data.api.dto.BusinessStats

import retrofit2.http.*

interface BusinessApi {
    @GET("business")
    suspend fun getAllBusinesses(): List<BusinessDto>

    @GET("business")
    suspend fun getAllBusinesses(
        @Query("superAdminId") superAdminId: String? = null,
        @Query("systemOwnerId") systemOwnerId: String? = null,
        @Query("status") status: String? = null
    ): List<BusinessDto>

    @GET("business/{id}")
    suspend fun getBusinessById(@Path("id") id: String): BusinessDto

    @POST("business")
    suspend fun createBusiness(@Body request: CreateBusinessRequest): retrofit2.Response<BusinessDto>

    @PUT("business/{id}")
    suspend fun updateBusiness(
        @Path("id") id: String,
        @Body request: UpdateBusinessRequest
    ): BusinessDto

    @DELETE("business/{id}")
    suspend fun deleteBusiness(@Path("id") id: String)

    @GET("user")
    suspend fun getBusinessUsers(@Query("businessId") businessId: String): List<UserDto>

    @GET("business/system-owner/{systemOwnerId}")
    suspend fun getBusinessesBySystemOwnerId(@Path("systemOwnerId") systemOwnerId: String): List<BusinessDto>

    @GET("business/super-admin/{superAdminId}")
    suspend fun getBusinessesBySuperAdminId(@Path("superAdminId") superAdminId: String): List<BusinessDto>

    @GET("business/stats/system-owner/{systemOwnerId}")
    suspend fun getSystemOwnerBusinessStats(@Path("systemOwnerId") systemOwnerId: String): BusinessStats

    @GET("business/stats/super-admin/{superAdminId}")
    suspend fun getSuperAdminBusinessStats(@Path("superAdminId") superAdminId: String): BusinessStats

    @GET("vehicle")
    suspend fun getBusinessVehicles(@Query("businessId") businessId: String): List<String>
}