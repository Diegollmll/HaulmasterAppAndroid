package app.forku

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.forku.data.local.TokenManager
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var tourPreferences: TourPreferences

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.statusBarColor = BackgroundGray.toArgb()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        super.onCreate(savedInstanceState)
        setContent {
            val loginState by loginViewModel.state.collectAsState()
            val hasToken = tokenManager.getToken() != null
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
                            }
                        )
                    }
                }
            }
        }
    }
}