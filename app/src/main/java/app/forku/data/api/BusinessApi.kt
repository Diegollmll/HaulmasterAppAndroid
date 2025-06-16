package app.forku.data.api

import app.forku.data.api.dto.user.UserDto
import app.forku.data.api.dto.business.BusinessDto
import retrofit2.Response
import retrofit2.http.*

interface BusinessApi {
    @GET("api/business/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getBusinessById(@Path("id") id: String): Response<BusinessDto>

    @GET("api/business/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllBusinesses(): Response<List<BusinessDto>>

    @GET("dataset/api/business/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getBusinessCount(): Response<Int>

    @POST("api/business")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun saveBusiness(@Body business: BusinessDto): Response<BusinessDto>

    @DELETE("dataset/api/business/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteBusiness(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/business/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllBusinessesDataset(): Response<List<BusinessDto>>

    @GET("dataset/api/business/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getBusinessByIdDataset(@Path("id") id: String): Response<BusinessDto>

    @POST("dataset/api/business")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun createBusinessDataset(@Body business: BusinessDto): Response<BusinessDto>

    @GET("api/business/{businessId}/user")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getBusinessUsers(@Path("businessId") businessId: String): Response<List<UserDto>>

    @GET("api/business/{businessId}/vehicle")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getBusinessVehicles(@Path("businessId") businessId: String): Response<List<String>>
}