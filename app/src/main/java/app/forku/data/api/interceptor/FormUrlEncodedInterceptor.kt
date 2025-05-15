package app.forku.data.api.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.JsonObject

@Singleton
class FormUrlEncodedInterceptor @Inject constructor() : Interceptor {
    private val gson = Gson()
    private val TAG = "FormUrlEncodedInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Only process POST requests with JSON content type
        if (originalRequest.method != "POST" || 
            originalRequest.body?.contentType()?.toString() != "application/json; charset=utf-8") {
            return chain.proceed(originalRequest)
        }

        try {
            // Get the original JSON body
            val bodyString = originalRequest.body?.let { body ->
                val buffer = okio.Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } ?: return chain.proceed(originalRequest)

            // Create the form-urlencoded body with 'entity' parameter
            val formBody = "entity=" + URLEncoder.encode(bodyString, "UTF-8")

            Log.d(TAG, "Converting request body to form-urlencoded: $formBody")

            // Create new request with form-urlencoded body
            val newRequest = originalRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .method(originalRequest.method, formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
                .build()

            return chain.proceed(newRequest)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting JSON to form-urlencoded", e)
            return chain.proceed(originalRequest)
        }
    }
} 