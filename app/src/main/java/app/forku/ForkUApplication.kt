package app.forku

import android.app.Application
import app.forku.data.datastore.AuthDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.FirebaseApp

@HiltAndroidApp
class ForkUApplication : Application() {
    @Inject
    lateinit var authDataStore: AuthDataStore

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase first
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        
        // Then initialize other components
        initializeAuth()
    }

    private fun initializeAuth() {
        CoroutineScope(Dispatchers.IO).launch {
            authDataStore.initializeToken()
        }
    }
} 