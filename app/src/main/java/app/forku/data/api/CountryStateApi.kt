package app.forku.data.api

import app.forku.data.api.dto.country.CountryStateDto
import retrofit2.Response
import retrofit2.http.*

interface CountryStateApi {
    @GET("api/countrystate/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllStates(): Response<List<CountryStateDto>>

    @GET("api/countrystate/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getStateById(@Path("id") id: String): Response<CountryStateDto>

    @POST("api/countrystate")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun createState(@Body state: CountryStateDto): Response<CountryStateDto>

    @POST("api/countrystate")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun updateState(@Body state: CountryStateDto): Response<CountryStateDto>

    @DELETE("dataset/api/countrystate/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteState(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/countrystate/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getStateCount(): Response<Int>

    // Additional endpoints for dataset format if needed
    @GET("dataset/api/countrystate/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllStatesDataset(): Response<List<CountryStateDto>>

    @GET("dataset/api/countrystate/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getStateByIdDataset(@Path("id") id: String): Response<CountryStateDto>

    @POST("dataset/api/countrystate")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun createStateDataset(@Body state: CountryStateDto): Response<CountryStateDto>
} 