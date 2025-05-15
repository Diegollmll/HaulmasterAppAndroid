package app.forku.data.api.dto.error

import com.google.gson.annotations.SerializedName

data class AuthErrorDto(
    @SerializedName("type")
    val type: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("status")
    val status: Int? = null,
    
    @SerializedName("detail")
    val detail: String? = null,
    
    @SerializedName("instance")
    val instance: String? = null,
    
    @SerializedName("traceId")
    val traceId: String? = null
) {
    fun isTokenExpired(): Boolean = 
        title == "expiredSecurityToken" || 
        detail?.contains("expiredSecurityToken", ignoreCase = true) == true

    fun isAuthError(): Boolean =
        status == 401 || status == 403 ||
        title?.contains("token", ignoreCase = true) == true ||
        detail?.contains("access denied", ignoreCase = true) == true
} 