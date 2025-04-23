package app.forku.data.api.interceptor

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            // Add any other default headers here
            .build()
        
        return chain.proceed(request)
    }
} 