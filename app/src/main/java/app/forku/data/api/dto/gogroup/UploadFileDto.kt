package app.forku.data.api.dto.gogroup

import com.google.gson.annotations.SerializedName

data class UploadFileDto(
    @SerializedName("FileName")
    val fileName: String,
    
    @SerializedName("FileContent")
    val fileContent: String, // Base64 encoded file content
    
    @SerializedName("ContentType")
    val contentType: String,
    
    @SerializedName("FileSize")
    val fileSize: Long,
    
    @SerializedName("UploadedAt")
    val uploadedAt: String? = null,
    
    @SerializedName("FileUrl")
    val fileUrl: String? = null
) 