package app.forku

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.forku.data.datastore.AuthDataStore
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.security.ProviderInstaller
import com.google.android.gms.security.ProviderInstaller.ProviderInstallListener
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltAndroidApp
class ForkUApplication : Application(), ProviderInstallListener {
    @Inject
    lateinit var authDataStore: AuthDataStore

    override fun onCreate() {
        super.onCreate()
        
        // Initialize security provider asynchronously
        ProviderInstaller.installIfNeededAsync(this, this)
        
        initializeAuth()
        // Log JWT expiration at app startup
        try {
            authDataStore.logTokenExpirationDate()
        } catch (e: Exception) {
            Log.e("HaulMaster application", "Failed to log token expiration", e)
        }
    }

    override fun onProviderInstalled() {
        // Security provider is up-to-date, initialize Google Services
        initializeGoogleServices()
    }

    override fun onProviderInstallFailed(errorCode: Int, intent: Intent?) {
        Log.e("SecurityProvider", "Provider install failed with code: $errorCode")
        GoogleApiAvailability.getInstance().let { availability ->
            if (availability.isUserResolvableError(errorCode)) {
                // Show dialog to update Google Play Services
                Toast.makeText(
                    this,
                    "Please update Google Play Services to use this app",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initializeGoogleServices() {
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Initialize Google Play Services
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(this)
            
            if (resultCode == ConnectionResult.SUCCESS) {
                // Google Play Services is available and up-to-date
                Log.d("GoogleServices", "Google Play Services is available")
            } else {
                Log.e("GoogleServices", "Google Play Services is not available: $resultCode")
                if (availability.isUserResolvableError(resultCode)) {
                    Toast.makeText(
                        this,
                        "Please update Google Play Services to use this app",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleServices", "Error initializing Google Services", e)
        }
    }


    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun initializeAuth() {
        CoroutineScope(Dispatchers.IO).launch {
            authDataStore.initializeApplicationToken()
        }
    }
} 