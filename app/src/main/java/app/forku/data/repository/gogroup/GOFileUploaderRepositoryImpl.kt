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
            android.util.Log.d("FileUpload", "[START] Subida de archivo iniciada: ${file.name}, mimeType=$mimeType, size=${file.length()} bytes")
            val headers = headerManager.getHeaders().getOrThrow()
            val csrfToken = headers.csrfToken
            val cookie = headers.cookie
            val requestFile = file.asRequestBody(null)
            val body = MultipartBody.Part.createFormData("files[]", file.name, requestFile)
            android.util.Log.d("FileUpload", "[STEP 1] Headers obtenidos, enviando request a FileUploaderApi")
            val response = api.uploadFile(body, csrfToken, cookie)
            android.util.Log.d("FileUpload", "[STEP 2] Respuesta recibida: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            if (!response.isSuccessful) {
                android.util.Log.e("FileUpload", "[ERROR] Fallo en la subida: code=${response.code()}")
                return@withContext Result.failure(Exception("Failed to upload file: \\${response.code()}"))
            }
            val uploadedFile = response.body()?.toDomain()
            if (uploadedFile == null) {
                android.util.Log.e("FileUpload", "[ERROR] Respuesta vacía al subir archivo")
                return@withContext Result.failure(Exception("Failed to upload file"))
            }
            android.util.Log.d("FileUpload", "[END] Archivo subido exitosamente: internalName=${uploadedFile.internalName}, fileSize=${uploadedFile.fileSize}")
            Result.success(uploadedFile)
        } catch (e: Exception) {
            android.util.Log.e("FileUpload", "[ERROR] Excepción en la subida: ${e.message}", e)
            Result.failure(e)
        }
    }
} 