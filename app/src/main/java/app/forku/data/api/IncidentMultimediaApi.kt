package app.forku.data.api

import app.forku.data.api.dto.incident.IncidentMultimediaDto
import retrofit2.Response
import retrofit2.http.*

interface IncidentMultimediaApi {
    @GET("api/incidentmultimedia/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getIncidentMultimediaById(@Path("Id") id: String): Response<IncidentMultimediaDto>

    @GET("api/incidentmultimedia/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllIncidentMultimedia(
        @Query("filter") filter: String? = null
    ): Response<List<IncidentMultimediaDto>>

    @GET("api/incidentmultimedia/byincidentid/{incidentId}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getIncidentMultimediaByIncidentId(
        @Path("IncidentId") incidentId: String
    ): Response<List<IncidentMultimediaDto>>

    @FormUrlEncoded
    @POST("api/incidentmultimedia")
    @Headers(
        "Accept: text/plain"
    )
    suspend fun saveIncidentMultimedia(
        @Field("entity") incidentMultimedia: String,
        @Query("BusinessId") businessId: String? = null,
        @Query("SiteId") siteId: String? = null
    ): Response<IncidentMultimediaDto>

    @DELETE("api/incidentmultimedia/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteIncidentMultimedia(@Path("Id") id: String): Response<Unit>

    @GET("dataset/api/incidentmultimedia/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getIncidentMultimediaCount(
        @Query("filter") filter: String? = null
    ): Response<Int>
} 