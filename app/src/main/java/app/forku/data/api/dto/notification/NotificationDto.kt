package app.forku.data.api.dto.notification

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class NotificationDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("isRead")
    val isRead: Boolean = false,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("priority")
    val priority: String,

    @SerializedName("data")
    val data: Map<String, String>? = null
) 