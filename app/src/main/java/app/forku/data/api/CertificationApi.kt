package app.forku.data.api

import app.forku.data.api.dto.certification.CertificationDto
import retrofit2.Response
import retrofit2.http.*

interface CertificationApi {
    @GET("certification")
    suspend fun getCertifications(
        @Query("userId") userId: String?
    ): Response<List<CertificationDto>>

    @GET("certification/{id}")
    suspend fun getCertificationById(
        @Path("id") id: String
    ): Response<CertificationDto>

    @POST("certification")
    suspend fun createCertification(
        @Body certification: CertificationDto,
        @Query("userId") userId: String
    ): Response<CertificationDto>

    @PUT("certification/{id}")
    suspend fun updateCertification(
        @Path("id") id: String,
        @Body certification: CertificationDto
    ): Response<CertificationDto>

    @DELETE("certification/{id}")
    suspend fun deleteCertification(
        @Path("id") id: String
    ): Response<Unit>
} 