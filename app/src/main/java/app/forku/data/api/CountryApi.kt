package app.forku.data.api

import app.forku.data.api.dto.country.CountryDto
import retrofit2.Response
import retrofit2.http.*

interface CountryApi {
    @GET("api/country/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllCountries(): Response<List<CountryDto>>
    
    @GET("api/country/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCountryById(@Path("id") id: String): Response<CountryDto>
    
    @POST("api/country")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun createCountry(@Body country: CountryDto): Response<CountryDto>
    
    @POST("api/country")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun updateCountry(@Body country: CountryDto): Response<CountryDto>
    
    @DELETE("dataset/api/country/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteCountry(@Path("id") id: String): Response<Unit>
    
    @GET("dataset/api/country/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCountryCount(): Response<Int>
} 