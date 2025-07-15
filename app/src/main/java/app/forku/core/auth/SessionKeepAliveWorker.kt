package app.forku.core.auth

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.datastore.AuthDataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker to maintain session when app is completely closed
 * Runs periodically to keep the session alive
 */
@HiltWorker
class SessionKeepAliveWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val goSecurityApi: GOSecurityProviderApi,
    private val authDataStore: AuthDataStore,
    private val headerManager: HeaderManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SessionKeepAliveWorker"
        const val WORK_NAME = "session_keep_alive_work"
        private const val MAX_RETRY_ATTEMPTS = 3
        
        /**
         * Schedule periodic session keep-alive work
         */
        fun schedulePeriodicWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<SessionKeepAliveWorker>(
                repeatInterval = 30, // 30 minutes
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 10, // 10 minutes flex time
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("session_maintenance")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            
            Log.d(TAG, "📅 Scheduled periodic session keep-alive work")
        }
        
        /**
         * Cancel scheduled work
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "❌ Cancelled session keep-alive work")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔄 Starting background session keep-alive work")
        
        try {
            val applicationToken = authDataStore.getApplicationToken()
            if (applicationToken.isNullOrBlank()) {
                Log.d(TAG, "⛔ No application token found - not starting keep-alive work")
                return@withContext Result.failure()
            }
            
            // Check token expiration
            val tokenExpiration = authDataStore.getTokenExpirationDate()
            if (tokenExpiration != null) {
                val timeUntilExpiration = tokenExpiration.time - System.currentTimeMillis()
                if (timeUntilExpiration <= 300_000L) { // 5 minutes
                    Log.w(TAG, "⏰ Token expires soon (${timeUntilExpiration / 1000}s) - attempting renewal")
                    val renewalSuccess = performTokenRenewal()
                    if (!renewalSuccess) {
                        Log.e(TAG, "❌ Token renewal failed - stopping work")
                        return@withContext Result.failure()
                    }
                }
            }
            
            // Perform keep-alive ping
            val keepAliveSuccess = performKeepAlive()
            
            if (keepAliveSuccess) {
                Log.d(TAG, "✅ Background session keep-alive successful")
                Result.success()
            } else {
                Log.w(TAG, "⚠️ Background session keep-alive failed - will retry")
                if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Result.retry()
                } else {
                    Log.e(TAG, "❌ Max retry attempts reached - stopping work")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in background session keep-alive", e)
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    /**
     * Perform keep-alive ping
     */
    private suspend fun performKeepAlive(): Boolean {
        return try {
            Log.d(TAG, "🔄 Performing background keep-alive ping...")
            
            val (csrfToken, cookie) = headerManager.getCsrfAndCookie()
            val response = goSecurityApi.keepAlive(csrfToken, cookie)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✅ Background keep-alive successful")
                true
            } else {
                Log.w(TAG, "⚠️ Background keep-alive failed: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Background keep-alive error", e)
            false
        }
    }
    
    /**
     * Perform token renewal
     */
    private suspend fun performTokenRenewal(): Boolean {
        return try {
            Log.d(TAG, "🔄 Performing background token renewal...")
            
            val authToken = authDataStore.getAuthenticationToken()
            if (authToken.isNullOrBlank()) {
                Log.w(TAG, "❌ No authentication token for renewal")
                return false
            }
            
            // Get headers for renewal with detailed debugging
            val headersResult = headerManager.getHeaders()
            if (headersResult.isFailure) {
                Log.w(TAG, "❌ Failed to get headers for renewal: ${headersResult.exceptionOrNull()?.message}")
                return false
            }
            
            val headers = headersResult.getOrNull()!!
            
            // 🚨 CRITICAL DEBUG: Check if ApplicationToken exists
            val applicationToken = headers.applicationToken ?: authDataStore.getApplicationToken()
            if (applicationToken == null) {
                Log.e(TAG, "🚨 CRITICAL: No ApplicationToken available for renewal!")
                Log.e(TAG, "💡 This explains server error 'No authentication token found'")
                return false
            }
            
            Log.d(TAG, "🔍 Using headers for renewal:")
            Log.d(TAG, "  - CSRF Token: ${headers.csrfToken.take(20)}...")
            Log.d(TAG, "  - ApplicationToken: ${applicationToken.take(20)}...")
            Log.d(TAG, "  - Raw Cookie: ${headers.cookie.take(100)}...")
            
            // 🔧 MANUAL COOKIE CONSTRUCTION to ensure ApplicationToken is included
            val debugCookie = "ApplicationToken=${applicationToken}; BearerToken=${applicationToken}; ${headers.cookie}"
            Log.d(TAG, "🔧 Background renewal cookie: ${debugCookie.take(150)}...")
            
            val response = goSecurityApi.renewToken(headers.csrfToken, debugCookie)
            
            if (response.isSuccessful) {
                response.body()?.let { tokenResponse ->
                    tokenResponse.getApplicationToken()?.let { appToken ->
                        authDataStore.saveApplicationToken(appToken)
                    }
                    tokenResponse.getAuthenticationToken()?.let { authToken ->
                        authDataStore.saveAuthenticationToken(authToken)
                    }
                    Log.d(TAG, "✅ Background token renewal successful")
                    true
                } ?: false
            } else {
                Log.w(TAG, "⚠️ Background token renewal failed: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Background token renewal error", e)
            false
        }
    }
} 