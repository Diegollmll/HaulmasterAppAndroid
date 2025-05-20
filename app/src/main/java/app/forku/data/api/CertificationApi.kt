package app.forku.data.api

import app.forku.data.api.dto.certification.CertificationDto
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers

interface CertificationApi {
    @GET("api/certification")
    suspend fun getCertifications(
        @Query("userId") userId: String?
    ): Response<List<CertificationDto>>

    @GET("api/certification/byid/{id}")
    suspend fun getCertificationById(
        @Path("id") id: String
    ): Response<CertificationDto>

    @FormUrlEncoded
    @POST("api/certification")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun createUpdateCertification(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String
    ): Response<CertificationDto>

    @DELETE("dataset/api/certification/{id}")
    suspend fun deleteCertification(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>

    @GET("api/certification/list")
    suspend fun getCertificationsByUserId(
        @Query("filter") filter: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<List<CertificationDto>>
} 