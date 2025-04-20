package app.forku.data.api


import app.forku.data.api.dto.EnergySourceDto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface EnergySourceApi {
    @GET("energy-source")
    suspend fun getAllEnergySources(): Response<List<EnergySourceDto>>

    @POST("energy-source")
    suspend fun createEnergySource(@Body energySource: EnergySourceDto): Response<EnergySourceDto>

    @PUT("energy-source/{id}")
    suspend fun updateEnergySource(@Path("id") id: String, @Body energySource: EnergySourceDto): Response<EnergySourceDto>

    @DELETE("energy-source/{id}")
    suspend fun deleteEnergySource(@Path("id") id: String): Response<Unit>
}