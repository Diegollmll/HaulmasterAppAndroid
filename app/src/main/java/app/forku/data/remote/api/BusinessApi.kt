package app.forku.data.remote.api

import app.forku.data.remote.dto.BusinessDto
import retrofit2.Response
import retrofit2.http.*

interface BusinessApi {
    @GET("business")
    suspend fun getAllBusinesses(): List<BusinessDto>

    @GET("business/{id}")
    suspend fun getBusinessById(@Path("id") id: String): BusinessDto

    @POST("business")
    suspend fun createBusiness(@Body business: CreateBusinessRequest): Response<BusinessDto>

    @PUT("business/{id}")
    suspend fun updateBusiness(
        @Path("id") id: String,
        @Query("name") name: String,
        @Query("status") status: String
    ): BusinessDto

    @DELETE("business/{id}")
    suspend fun deleteBusiness(@Path("id") id: String)

    @POST("business/{businessId}/users/{userId}")
    suspend fun assignUserToBusiness(
        @Path("userId") userId: String,
        @Path("businessId") businessId: String
    )

    @DELETE("business/{businessId}/users/{userId}")
    suspend fun removeUserFromBusiness(
        @Path("userId") userId: String,
        @Path("businessId") businessId: String
    )

    @GET("business/{businessId}/users")
    suspend fun getBusinessUsers(@Path("businessId") businessId: String): List<String>

    @GET("business/{businessId}/vehicles")
    suspend fun getBusinessVehicles(@Path("businessId") businessId: String): List<String>
}