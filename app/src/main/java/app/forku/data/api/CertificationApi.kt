package app.forku.data.api

import app.forku.data.api.dto.certification.CertificationDto
import retrofit2.Response
import retrofit2.http.*

interface CertificationApi {
    @GET("certifications")
    suspend fun getCertifications(
        @Query("userId") userId: String?
    ): Response<List<CertificationDto>>

    @GET("certifications/{id}")
    suspend fun getCertificationById(
        @Path("id") id: String
    ): Response<CertificationDto>

    @POST("certifications")
    suspend fun createCertification(
        @Body certification: CertificationDto,
        @Query("userId") userId: String
    ): Response<CertificationDto>

    @PUT("certifications/{id}")
    suspend fun updateCertification(
        @Path("id") id: String,
        @Body certification: CertificationDto
    ): Response<CertificationDto>

    @DELETE("certifications/{id}")
    suspend fun deleteCertification(
        @Path("id") id: String
    ): Response<Unit>
} 