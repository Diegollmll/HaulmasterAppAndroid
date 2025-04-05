package app.forku.data.api

import app.forku.data.api.dto.incident.IncidentDto
import retrofit2.Response
import retrofit2.http.*

interface IncidentApi {
    @POST("incidents")
    suspend fun reportIncident(
        @Body incident: IncidentDto
    ): Response<IncidentDto>

    @GET("incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @GET("incidents/{id}")
    suspend fun getIncidentById(
        @Path("id") id: String
    ): Response<IncidentDto>
} 