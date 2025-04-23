package app.forku.data.api.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {
    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_DELAY = 1000L // 1 second
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var retryCount = 0
        var response: Response? = null
        var exception: IOException? = null
        
        while (retryCount < MAX_RETRIES) {
            try {
                // If this isn't the first attempt, and we had a previous response, close it
                response?.close()
                
                response = chain.proceed(chain.request())
                
                // If the response is successful, return it
                if (response.isSuccessful) {
                    return response
                }
                
                // If the response wasn't successful, close it and prepare for retry
                response.close()
                
            } catch (e: IOException) {
                exception = e
                Log.w("RetryInterceptor", "Attempt ${retryCount + 1} failed", e)
            }
            
            retryCount++
            
            if (retryCount < MAX_RETRIES) {
                val backoffDelay = INITIAL_BACKOFF_DELAY * (1 shl (retryCount - 1))
                Log.d("RetryInterceptor", "Retrying in $backoffDelay ms")
                Thread.sleep(backoffDelay)
            }
        }
        
        // If we got here, all retries failed
        throw exception ?: IOException("Request failed after $MAX_RETRIES retries")
    }
} 