package app.forku.data.api

import app.forku.data.api.dto.country.StateDto
import retrofit2.Response
import retrofit2.http.*

interface StateApi {
    @GET("country-state")
    suspend fun getAllStates(): Response<List<StateDto>>

    @GET("country-state")
    suspend fun getStatesByCountry(@Query("countryId") countryId: String): Response<List<StateDto>>

    @GET("country-state/{id}")
    suspend fun getStateById(@Path("id") id: String): Response<StateDto>

    @POST("country-state")
    suspend fun createState(@Body state: StateDto): Response<StateDto>

    @PUT("country-state/{id}")
    suspend fun updateState(
        @Path("id") id: String,
        @Body state: StateDto
    ): Response<StateDto>

    @DELETE("country-state/{id}")
    suspend fun deleteState(@Path("id") id: String): Response<Unit>
} 