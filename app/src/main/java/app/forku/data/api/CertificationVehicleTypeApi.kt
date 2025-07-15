package app.forku.data.api

import app.forku.data.api.dto.certification.CertificationVehicleTypeDto
import retrofit2.Response
import retrofit2.http.*

interface CertificationVehicleTypeApi {
    
    @GET("dataset/api/certificationvehicletype/byid/{id}")
    suspend fun getCertificationVehicleTypeByIdDataset(
        @Path("id") id: String,
        @Query("businessId") businessId: String? = null
    ): Response<CertificationVehicleTypeDto>

    @GET("api/certificationvehicletype/byid/{id}")
    suspend fun getCertificationVehicleTypeById(
        @Path("id") id: String,
        @Query("businessId") businessId: String? = null
    ): Response<CertificationVehicleTypeDto>

    @GET("dataset/api/certificationvehicletype/list")
    suspend fun getCertificationVehicleTypesDataset(
        @Query("filter") filter: String? = null,
        @Query("businessId") businessId: String? = null
    ): Response<List<CertificationVehicleTypeDto>>

    @GET("api/certificationvehicletype/list")
    suspend fun getCertificationVehicleTypes(
        @Query("filter") filter: String? = null,
        @Query("businessId") businessId: String? = null
    ): Response<List<CertificationVehicleTypeDto>>

    @GET("dataset/api/certificationvehicletype/count")
    suspend fun getCertificationVehicleTypeCount(
        @Query("filter") filter: String? = null,
        @Query("businessId") businessId: String? = null
    ): Response<Int>

    @FormUrlEncoded
    @POST("dataset/api/certificationvehicletype")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun createUpdateCertificationVehicleTypeDataset(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String,
        @Query("businessId") businessId: String? = null
    ): Response<CertificationVehicleTypeDto>

    @FormUrlEncoded
    @POST("api/certificationvehicletype")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun createUpdateCertificationVehicleType(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Field("entity") entity: String,
        @Query("businessId") businessId: String? = null
    ): Response<CertificationVehicleTypeDto>

    @DELETE("dataset/api/certificationvehicletype")
    suspend fun deleteCertificationVehicleTypeDataset(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Query("id") id: String,
        @Query("businessId") businessId: String? = null
    ): Response<Unit>

    @DELETE("dataset/api/certificationvehicletype/{id}")
    suspend fun deleteCertificationVehicleType(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Query("businessId") businessId: String? = null
    ): Response<Unit>

    @DELETE("api/certificationvehicletype/{id}")
    suspend fun deleteCertificationVehicleTypeApi(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String,
        @Query("businessId") businessId: String? = null
    ): Response<Unit>
} 