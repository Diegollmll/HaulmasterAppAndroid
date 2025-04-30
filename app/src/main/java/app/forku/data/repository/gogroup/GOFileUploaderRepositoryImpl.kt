package app.forku.data.repository.gogroup

import app.forku.data.api.GOFileUploaderApi
import app.forku.data.mapper.toDomain
import app.forku.data.mapper.toMultipartBodyPart
import app.forku.domain.model.gogroup.UploadFile
import app.forku.domain.repository.gogroup.GOFileUploaderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GOFileUploaderRepositoryImpl @Inject constructor(
    private val api: GOFileUploaderApi
) : GOFileUploaderRepository {

    override suspend fun uploadFile(file: UploadFile): Result<UploadFile> = withContext(Dispatchers.IO) {
        try {
            val response = api.uploadFile(file.toMultipartBodyPart())
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to upload file: ${response.code()}"))
            }

            val uploadedFile = response.body()?.toDomain()
                ?: return@withContext Result.failure(Exception("Failed to upload file"))

            Result.success(uploadedFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 