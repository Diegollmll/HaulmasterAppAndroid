package app.forku.core.utils

import app.forku.core.Constants
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object ApiUtils {
    private const val APP_PREFIX = "forkuapp-de6b98b5-4402-4a8f-891b-70b3591df162"

    fun buildApiUrl(scheme: String, host: String, path: String): HttpUrl {
        return HttpUrl.Builder()
            .scheme(scheme)
            .host(host)
            .addPathSegment(APP_PREFIX)
            .addPathSegment("api")
            .addPathSegments(path.trimStart('/'))
            .build()
    }

    fun buildApiUrl(baseUrl: HttpUrl, path: String): HttpUrl {
        return baseUrl.newBuilder()
            .addPathSegment("api")
            .addPathSegments(path.trimStart('/'))
            .build()
    }

    fun getBaseUrl(): HttpUrl {
        return Constants.BASE_URL.toHttpUrl()
    }
}