package app.forku.data.api.dto

import app.forku.data.api.dto.error.AuthErrorDto
import retrofit2.HttpException
import retrofit2.Response

sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val exception: Exception) : ApiResponse<Nothing>()
    data class AuthError(val error: AuthErrorDto) : ApiResponse<Nothing>()
}

fun <T> Response<T>.toApiResponse(): ApiResponse<T> {
    return try {
        if (isSuccessful) {
            val body = body()
            if (body != null) {
                ApiResponse.Success(body)
            } else {
                ApiResponse.Error(Exception("Response body is null"))
            }
        } else {
            val errorBody = errorBody()?.string()
            if (errorBody != null) {
                try {
                    val authError = com.google.gson.Gson().fromJson(errorBody, AuthErrorDto::class.java)
                    if (authError.isTokenExpired() || authError.isAuthError()) {
                        ApiResponse.AuthError(authError)
                    } else {
                        ApiResponse.Error(HttpException(this))
                    }
                } catch (e: Exception) {
                    ApiResponse.Error(HttpException(this))
                }
            } else {
                ApiResponse.Error(HttpException(this))
            }
        }
    } catch (e: Exception) {
        ApiResponse.Error(e)
    }
} 