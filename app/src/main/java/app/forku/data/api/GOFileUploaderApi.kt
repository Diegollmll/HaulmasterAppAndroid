package app.forku.data.api

import app.forku.data.api.dto.gofileuploader.UploadFileResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface for GO File Uploader endpoints.
 * All endpoints follow the pattern /api/gofileuploader/* */ for file operations.
 */
interface GOFileUploaderApi {
    /**
     * Upload a file to the GO Platform
     * @param file The file to upload as multipart form data
     * @return Response containing the uploaded file information
     */
    @Multipart
    @POST("api/gofileuploader/uploadfile")
    @Headers(
        "Accept: text/plain"
    )
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadFileResponseDto>
} 