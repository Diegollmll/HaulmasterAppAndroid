package app.forku.data.api

import app.forku.data.api.dto.certification.CertificationMultimediaDto
import retrofit2.Response
import retrofit2.http.*

interface CertificationMultimediaApi {
    @GET("api/certificationmultimedia/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCertificationMultimediaById(@Path("id") id: String): Response<CertificationMultimediaDto>

    @GET("api/certificationmultimedia/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllCertificationMultimedia(
        @Query("filter") filter: String? = null
    ): Response<List<CertificationMultimediaDto>>

    @GET("api/certificationmultimedia/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCertificationMultimediaByCertificationId(
        @Query("filter") filter: String
    ): Response<List<CertificationMultimediaDto>>

    @FormUrlEncoded
    @POST("api/certificationmultimedia")
    @Headers(
        "Accept: text/plain"
    )
    suspend fun saveCertificationMultimedia(
        @Field("entity") certificationMultimedia: String,
        @Query("businessId") businessId: String? = null,
        @Query("siteId") siteId: String? = null
    ): Response<CertificationMultimediaDto>

    @DELETE("api/certificationmultimedia/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteCertificationMultimedia(@Path("id") id: String): Response<Unit>

    @GET("dataset/api/certificationmultimedia/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getCertificationMultimediaCount(
        @Query("filter") filter: String? = null
    ): Response<Int>
} 