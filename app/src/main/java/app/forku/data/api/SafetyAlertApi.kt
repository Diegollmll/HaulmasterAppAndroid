package app.forku.data.api

import app.forku.data.api.dto.safetyalert.SafetyAlertDto
import retrofit2.http.*
import retrofit2.Response

interface SafetyAlertApi {
    @GET("api/safetyalert/byid/{id}")
    suspend fun getSafetyAlertById(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<SafetyAlertDto>

    @GET("api/safetyalert/list")
    suspend fun getSafetyAlertList(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<List<SafetyAlertDto>>

    @FormUrlEncoded
    @POST("api/safetyalert")
    suspend fun saveSafetyAlert(
        @Field("entity") entity: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<SafetyAlertDto>

    @DELETE("api/safetyalert")
    suspend fun deleteSafetyAlert(
        @Body alert: SafetyAlertDto,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>

    // Dataset endpoints
    @GET("dataset/api/safetyalert/byid/{id}")
    suspend fun getDatasetSafetyAlertById(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Any>

    @GET("dataset/api/safetyalert/list")
    suspend fun getDatasetSafetyAlertList(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Any>

    @GET("dataset/api/safetyalert/count")
    suspend fun getDatasetSafetyAlertCount(
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Accept") accept: String = "text/plain"
    ): Response<Int>

    @POST("dataset/api/safetyalert")
    suspend fun saveDatasetSafetyAlert(
        @Body alert: Any,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Any>

    @DELETE("dataset/api/safetyalert")
    suspend fun deleteDatasetSafetyAlert(
        @Body alert: Any,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>

    @DELETE("dataset/api/safetyalert/{id}")
    suspend fun deleteDatasetSafetyAlertById(
        @Path("id") id: String,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<Unit>
} 