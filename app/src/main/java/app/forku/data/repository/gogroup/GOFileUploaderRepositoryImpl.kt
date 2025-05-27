package app.forku.data.repository.gogroup

import app.forku.data.api.FileUploaderApi
import app.forku.data.mapper.toDomain
import app.forku.domain.model.gogroup.UploadFile
import app.forku.domain.repository.gogroup.GOFileUploaderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.core.auth.HeaderManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Singleton
class GOFileUploaderRepositoryImpl @Inject constructor(
    private val api: FileUploaderApi,
    private val headerManager: HeaderManager
) : GOFileUploaderRepository {

    override suspend fun uploadFile(file: File, mimeType: String): Result<UploadFile> = withContext(Dispatchers.IO) {
        try {
            val headers = headerManager.getHeaders().getOrThrow()
            val csrfToken = headers.csrfToken
            val cookie = headers.cookie
            val requestFile = file.asRequestBody(null)
            val body = MultipartBody.Part.createFormData("files[]", file.name, requestFile)
            val response = api.uploadFile(body, csrfToken, cookie)
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to upload file: \\${response.code()}"))
            }
            val uploadedFile = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Failed to upload file"))
            Result.success(uploadedFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 