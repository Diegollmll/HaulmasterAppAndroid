package app.forku

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.forku.data.datastore.AuthDataStore
import app.forku.data.local.TourPreferences
import app.forku.presentation.user.login.LoginState
import app.forku.presentation.user.login.LoginViewModel
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.LoadingScreen
import app.forku.presentation.common.theme.ForkUTheme
import app.forku.presentation.navigation.NavGraph
import app.forku.presentation.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.view.View
import androidx.compose.ui.graphics.toArgb
import app.forku.presentation.common.theme.BackgroundGray
import androidx.lifecycle.lifecycleScope
import app.forku.core.network.NetworkConnectivityManager
import kotlinx.coroutines.launch
import app.forku.core.location.LocationManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authDataStore: AuthDataStore

    @Inject
    lateinit var tourPreferences: TourPreferences

    @Inject
    lateinit var networkManager: NetworkConnectivityManager

    @Inject
    lateinit var locationManager: LocationManager

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = BackgroundGray.toArgb()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        super.onCreate(savedInstanceState)
        
        // Inicializar el token al inicio
        lifecycleScope.launch {
            authDataStore.initializeToken()
        }

        setContent {
            val loginState by loginViewModel.state.collectAsState()
            val hasToken = authDataStore.getToken() != null
            val tourCompleted = tourPreferences.hasTourCompleted()

            ForkUTheme {
                when (loginState) {
                    is LoginState.Loading -> LoadingScreen()
                    is LoginState.Error -> {
                        val error = (loginState as LoginState.Error).message
                        ErrorScreen(
                            message = error,
                            onRetry = { loginViewModel.resetState() }
                        )
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        NavGraph(
                            startDestination = when {
                                !tourCompleted -> Screen.Tour.route
                                loginState is LoginState.Success || hasToken -> Screen.Dashboard.route
                                else -> Screen.Login.route
                            },
                            networkManager = networkManager,
                            locationManager = locationManager
                        )
                    }
                }
            }
        }
    }
}