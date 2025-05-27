package app.forku.data.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Header
import app.forku.data.api.dto.gofileuploader.UploadFileResponseDto

interface FileUploaderApi {
    @Multipart
    @POST("api/gofileuploader/uploadfile")
    suspend fun uploadFile(
        @Part files: MultipartBody.Part,
        @Header("X-CSRF-TOKEN") csrfToken: String,
        @Header("Cookie") cookie: String
    ): Response<UploadFileResponseDto>
} 