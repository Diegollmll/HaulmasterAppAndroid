package app.forku.data.api.interceptor

import app.forku.data.datastore.GOServicesPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsrfTokenInterceptor @Inject constructor(
    private val preferences: GOServicesPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { preferences.csrfToken.first() }
        
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("X-CSRF-TOKEN", token)
                .build()
        } else {
            chain.request()
        }
        
        return chain.proceed(request)
    }
} 