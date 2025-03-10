package app.forku

import android.app.Application
import app.forku.data.datastore.AuthDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ForkUApplication : Application() {
    @Inject
    lateinit var authDataStore: AuthDataStore

    override fun onCreate() {
        super.onCreate()
        initializeAuth()
    }

    private fun initializeAuth() {
        CoroutineScope(Dispatchers.IO).launch {
            authDataStore.initializeToken()
        }
    }
} 