package app.forku.data.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.min
import kotlin.math.pow

class RetryInterceptor : Interceptor {
    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_DELAY = 1000L // 1 second
        private const val MAX_BACKOFF_DELAY = 10000L // 10 seconds
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var retryCount = 0
        var response: Response? = null
        var exception: IOException? = null

        while (retryCount < MAX_RETRIES) {
            try {
                // If this is a retry, and we have a previous response, close it
                response?.close()
                
                response = chain.proceed(chain.request())
                
                // If the response is successful or not a rate limit error, return it
                if (response.isSuccessful || response.code != 429) {
                    return response
                }

                // Close the rate-limited response
                response.close()

                // Calculate backoff delay with exponential increase
                val backoffDelay = calculateBackoffDelay(retryCount)
                Thread.sleep(backoffDelay)
                
                retryCount++
            } catch (e: IOException) {
                exception = e
                retryCount++
                if (retryCount == MAX_RETRIES) {
                    throw e
                }
            }
        }

        // If we've exhausted retries and have a response, return it
        response?.let { return it }
        
        // If we have no response but have an exception, throw it
        throw exception ?: IOException("Request failed after $MAX_RETRIES retries")
    }

    private fun calculateBackoffDelay(retryCount: Int): Long {
        val backoffDelay = INITIAL_BACKOFF_DELAY * 2.0.pow(retryCount.toDouble())
        return min(backoffDelay.toLong(), MAX_BACKOFF_DELAY)
    }
} 