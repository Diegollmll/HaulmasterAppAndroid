package app.forku.data.api

import app.forku.data.api.dto.country.CountryDto
import retrofit2.Response
import retrofit2.http.*

interface CountryApi {
    @GET("country")
    suspend fun getAllCountries(): Response<List<CountryDto>>
    
    @GET("country/{id}")
    suspend fun getCountry(@Path("id") id: String): Response<CountryDto>
    
    @POST("country")
    suspend fun createCountry(@Body country: CountryDto): Response<CountryDto>
    
    @PUT("country/{id}")
    suspend fun updateCountry(
        @Path("id") id: String,
        @Body country: CountryDto
    ): Response<CountryDto>
    
    @DELETE("country/{id}")
    suspend fun deleteCountry(@Path("id") id: String): Response<Unit>
} 