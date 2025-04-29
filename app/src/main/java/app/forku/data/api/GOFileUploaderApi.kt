package app.forku.data.api

import app.forku.data.api.dto.gogroup.UploadFileDto
import retrofit2.Response
import retrofit2.http.*

interface GOFileUploaderApi {
    /**
     * Upload a file to the GO Platform
     * @param file File data to upload
     * @return Response containing the uploaded file information
     */
    @POST("api/gofileuploader/uploadfile")
    suspend fun uploadFile(@Body file: UploadFileDto): Response<UploadFileDto>
} 