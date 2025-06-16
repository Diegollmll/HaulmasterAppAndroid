package app.forku.data.api

import app.forku.data.api.dto.multimedia.MultimediaDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MultimediaApi {
    @GET("api/multimedia/byid/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getMultimediaById(
        @Path("id") id: String,
        @Query("businessId") businessId: String? = null
    ): Response<MultimediaDto>

    @GET("api/multimedia/list")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getAllMultimedia(
        @Query("filter") filter: String? = null,
        @Query("businessId") businessId: String? = null
    ): Response<List<MultimediaDto>>

    @GET("api/multimedia/file/{id}/Image")
    @Headers(
        "Accept: image/*"
    )
    suspend fun getMultimediaImage(
        @Path("id") id: String,
        @Query("businessId") businessId: String? = null
    ): Response<ByteArray>

    @POST("api/multimedia")
    @Headers(
        "Content-Type: application/x-www-form-urlencoded",
        "Accept: text/plain"
    )
    suspend fun saveMultimedia(
        @Field("entity") multimedia: String,
        @Query("businessId") businessId: String? = null
    ): Response<MultimediaDto>

    @DELETE("api/multimedia/{id}")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun deleteMultimedia(
        @Path("id") id: String,
        @Query("businessId") businessId: String? = null
    ): Response<Unit>

    @GET("dataset/api/multimedia/count")
    @Headers(
        "Content-Type: application/json",
        "Accept: text/plain"
    )
    suspend fun getMultimediaCount(
        @Query("filter") filter: String? = null,
        @Query("businessId") businessId: String? = null
    ): Response<Int>
} 