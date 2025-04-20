package app.forku.data.api.dto

import com.google.gson.annotations.SerializedName

data class FeedbackDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("userId") val userId: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) 