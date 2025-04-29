package app.forku.data.api.interceptor

import android.util.Log
import app.forku.data.datastore.AuthDataStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsrfTokenInterceptor @Inject constructor(
    private val authDataStore: AuthDataStore
) : Interceptor {
    private var csrfCookie: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val csrfToken = authDataStore.getCsrfToken()
        
        Log.d("CsrfTokenInterceptor", """
            Request details:
            - URL: ${originalRequest.url}
            - Method: ${originalRequest.method}
            - Content-Type: ${originalRequest.header("Content-Type")}
            - CSRF Token: ${csrfToken?.take(10)}...
            - CSRF Cookie: ${csrfCookie?.take(10)}...
        """.trimIndent())

        val builder = originalRequest.newBuilder()
        
        if (csrfToken != null) {
            builder.addHeader("X-CSRF-TOKEN", csrfToken)
            builder.addHeader("RequestVerificationToken", csrfToken)
        } else {
            Log.w("CsrfTokenInterceptor", "No CSRF token available!")
        }
        
        // Add the CSRF cookie if available
        if (csrfCookie != null) {
            builder.addHeader("Cookie", csrfCookie!!)
        }

        val response = chain.proceed(builder.build())
        
        // Store the CSRF cookie from the response if present
        response.headers("Set-Cookie").forEach { cookie ->
            if (cookie.startsWith(".AspNetCore.Antiforgery")) {
                csrfCookie = cookie.split(";")[0]
                Log.d("CsrfTokenInterceptor", "Stored CSRF cookie: ${csrfCookie?.take(10)}...")
            }
        }
        
        Log.d("CsrfTokenInterceptor", """
            Response details:
            - Code: ${response.code}
            - Message: ${response.message}
            - Headers: ${response.headers}
        """.trimIndent())

        return response
    }
} 