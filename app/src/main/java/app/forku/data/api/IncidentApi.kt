package app.forku.data.api

import app.forku.data.api.dto.incident.IncidentDto
import retrofit2.Response
import retrofit2.http.*

interface IncidentApi {
    @GET("api/incident/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllIncidents(
        @Query("filter") filter: String? = null,
        @Query("include") include: String? = null
    ): Response<List<IncidentDto>>

    @GET("api/incident/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getIncidentById(@Path("id") id: String): Response<IncidentDto>

    @FormUrlEncoded
    @POST("api/incident")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun saveIncident(
        @Field("entity") incident: String,
        @Query("businessId") businessId: String? = null
    ): Response<IncidentDto>

    @DELETE("dataset/api/incident/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteIncident(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/incident/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getIncidentCount(
        @Query("filter") filter: String? = null
    ): Response<Int>

    // Add endpoints for incident types and multimedia if needed
    // Example:
    // @GET("api/incidenttype/list")
    // suspend fun getIncidentTypes(): Response<List<IncidentTypeDto>>
    // @GET("api/incidentmultimedia/list")
    // suspend fun getIncidentMultimedia(): Response<List<IncidentMultimediaDto>>
} 