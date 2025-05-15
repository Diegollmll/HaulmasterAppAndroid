package app.forku.data.api.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import app.forku.data.api.dto.error.AuthErrorDto
import com.google.gson.Gson
import java.net.HttpURLConnection
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource

private const val MAX_RETRIES = 3
private const val INITIAL_BACKOFF_DELAY = 1000L // 1 second

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {
    private val gson = Gson()
    private val TAG = "RetryInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        var retryCount = 0
        var currentDelay = INITIAL_BACKOFF_DELAY

        while (true) {
            try {
                val response = chain.proceed(chain.request())
                
                // Don't retry on authentication errors (let AuthInterceptor handle them)
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED || 
                    response.code == HttpURLConnection.HTTP_FORBIDDEN) {
                    
                    // Check if it's a token expiry error without consuming the response body
                    val source = response.body?.source()
                    if (source != null) {
                        // Buffer the source so we can read it multiple times
                        val bufferedSource = source.buffer.clone()
                        val errorBody = bufferedSource.readUtf8()
                        
                        try {
                            val authError = gson.fromJson(errorBody, AuthErrorDto::class.java)
                            if (authError?.isTokenExpired() == true || authError?.isAuthError() == true) {
                                Log.d(TAG, "Auth error detected, letting AuthInterceptor handle it")
                                // Create a new response with the buffered body
                                return response.newBuilder()
                                    .body(ResponseBody.create(
                                        response.body?.contentType(),
                                        errorBody
                                    ))
                                    .build()
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse error response as AuthErrorDto")
                        }
                    }
                }

                // For other error codes, attempt retry
                if (!response.isSuccessful && retryCount < MAX_RETRIES) {
                    response.close()
                    retryCount++
                    Log.d(TAG, "Request failed (${response.code}), attempt $retryCount of $MAX_RETRIES")
                    Thread.sleep(currentDelay)
                    currentDelay *= 2 // Exponential backoff
                    continue
                }
                
                return response
            } catch (e: IOException) {
                if (retryCount >= MAX_RETRIES) {
                    Log.e(TAG, "Max retries reached, throwing last error", e)
                    throw e
                }
                
                retryCount++
                Log.d(TAG, "Request failed with IOException, attempt $retryCount of $MAX_RETRIES", e)
                Thread.sleep(currentDelay)
                currentDelay *= 2 // Exponential backoff
            }
        }
    }
} 