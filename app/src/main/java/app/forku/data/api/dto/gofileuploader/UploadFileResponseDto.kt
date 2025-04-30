package app.forku.data.api.dto.gofileuploader

import com.google.gson.annotations.SerializedName

/**
 * Response DTO for file upload endpoint
 */
data class UploadFileResponseDto(
    @SerializedName("internalName")
    val internalName: String,

    @SerializedName("clientName")
    val clientName: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("type")
    val type: String
) 