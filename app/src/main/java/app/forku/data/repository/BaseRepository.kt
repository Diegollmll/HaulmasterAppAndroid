package app.forku.data.repository

import android.util.Log
import app.forku.core.auth.TokenErrorHandler
import app.forku.data.datastore.AuthDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

abstract class BaseRepository(
    protected val authDataStore: AuthDataStore,
    protected val tokenErrorHandler: TokenErrorHandler
) {
    private val TAG = "BaseRepository"

    /**
     * Execute a network call with proper error handling
     * @param call The suspend function to execute
     * @return Result containing the response data or error
     */
    protected suspend fun <T> executeApiCall(
        call: suspend () -> Response<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Response body is null"))
            } else {
                when (response.code()) {
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        val exception = HttpException(response)
                        tokenErrorHandler.processError(exception)
                        Result.failure(exception)
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "API error: ${response.code()} - $errorBody")
                        Result.failure(Exception("API error: ${response.code()}"))
                    }
                }
            }
        } catch (e: Exception) {
            if (e is HttpException) {
                tokenErrorHandler.processError(e)
            }
            Log.e(TAG, "API call failed", e)
            Result.failure(e)
        }
    }

    /**
     * Execute a network call that returns a list, with proper error handling
     * @param call The suspend function to execute
     * @return The list of results or an empty list on error
     */
    protected suspend fun <T> executeApiCallForList(
        call: suspend () -> Response<List<T>>
    ): List<T> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                when (response.code()) {
                    HttpURLConnection.HTTP_UNAUTHORIZED,
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        val exception = HttpException(response)
                        tokenErrorHandler.processError(exception)
                        emptyList()
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "API error: ${response.code()} - $errorBody")
                        emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            if (e is HttpException) {
                tokenErrorHandler.processError(e)
            }
            Log.e(TAG, "API call failed", e)
            emptyList()
        }
    }
} 