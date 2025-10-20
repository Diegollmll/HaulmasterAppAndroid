package app.forku.presentation.user.login

import android.util.Log
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.domain.model.user.User
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.navigation.Screen
import app.forku.core.auth.TokenErrorHandler
import app.forku.core.auth.AuthenticationState
import androidx.compose.ui.res.painterResource
import app.forku.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import app.forku.presentation.common.viewmodel.AdminSharedFiltersViewModel
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (User) -> Unit,
    networkManager: NetworkConnectivityManager,
    navController: NavController,
    tokenErrorHandler: TokenErrorHandler
) {
    val owner = LocalViewModelStoreOwner.current
    val sharedFiltersViewModel: AdminSharedFiltersViewModel = hiltViewModel(viewModelStoreOwner = owner!!)

    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- Obtener SessionKeepAliveManager usando EntryPoint (igual que en BaseScreen) ---
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionKeepAliveManager = remember {
        try {
            (context as? androidx.activity.ComponentActivity)?.let { activity ->
                dagger.hilt.android.EntryPointAccessors.fromActivity(
                    activity,
                    app.forku.presentation.common.components.BaseScreenEntryPoint::class.java
                ).sessionKeepAliveManager()
            }
        } catch (e: Exception) {
            android.util.Log.w("LoginScreen", "Could not access SessionKeepAliveManager", e)
            null
        }
    }

    // Reset state when entering screen
    LaunchedEffect(Unit) {
        sharedFiltersViewModel.clearFilters()
        viewModel.resetState()
    }

    // Handle login success
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            val user = (state as LoginState.Success).user
            Log.d("LoginScreen", "[FLOW] LoginState.Success - user: $user, role: ${user.role}")
            // --- Resetear evento de sesión expirada ---
            //sessionKeepAliveManager?.resetSessionExpiredEvent("LoginScreen: login success")
            Log.d("LoginScreen", "[FLOW] LoginState.Success ZZZZZ A")
            // Ensure we're in a clean state before navigation
            tokenErrorHandler.resetAuthenticationState()
            Log.d("LoginScreen", "[FLOW] LoginState.Success ZZZZZ B")
            Log.d("LoginScreen", "[FLOW] Calling onLoginSuccess(user) with role: ${user.role}")
            Log.d("LoginScreen", "[FLOW] LoginState.Success ZZZZZ C")
            onLoginSuccess(user)
            Log.d("LoginScreen", "[FLOW] LoginState.Success ZZZZZ D")
        } else if (state is LoginState.RequiresPreferencesSetup) {
            Log.d("LoginScreen", "[FLOW] RequiresPreferencesSetup")
            val user = (state as LoginState.RequiresPreferencesSetup).user
            Log.d("LoginScreen", "[FLOW] LoginState.RequiresPreferencesSetup - user: $user, role: ${user.role}")
            // Ensure we're in a clean state before navigation
            tokenErrorHandler.resetAuthenticationState()
            Log.d("LoginScreen", "[FLOW] Navigating to UserPreferencesSetup for user: ${user.id}")
            // Navigate to UserPreferencesSetup instead of SystemSettings
                            navController.navigate(Screen.UserPreferencesSetup.createRoute(showBack = false)) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.background_gray))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Centrar todo el contenido
        ) {
            /* TODO: Handle back navigation */
            Spacer(modifier = Modifier.height(32.dp))

            // Logo de la app
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_haulmaster),
                    contentDescription = "Haulmaster Logo",
                    modifier = Modifier.size(140.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "Welcome back",
                color = colorResource(id = R.color.carbon_gray),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Enter your details below.",
                color = colorResource(id = R.color.carbon_gray_light),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 32.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally), // Centrar subtítulo
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column() {
                Text(
                    text = "Email address",
                    color = colorResource(id = R.color.carbon_gray_light),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF8F9FA), // gris muy claro arriba
                                    Color(0xFFFFFFFF)  // blanco abajo
                                )
                            ),
                            shape = RoundedCornerShape(12.dp) // bordes redondeados
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0), // borde gris clarito (opcional)
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Enter your email address") },
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent, // 👈 fondo lo maneja el Box
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column() {
                Text(
                    text = "Password",
                    color = colorResource(id = R.color.carbon_gray_light),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF8F9FA), // gris muy claro arriba
                                    Color(0xFFFFFFFF)  // blanco abajo
                                )
                            ),
                            shape = RoundedCornerShape(12.dp) // bordes redondeados
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0), // borde gris clarito (opcional)
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {

                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Enter your password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent, // 👈 fondo lo maneja el Box
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = {
                    Log.d("LoginScreen", "Login button clicked for user: $username")
                    viewModel.login(username, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp) // Padding horizontal
                    .height(56.dp), // Altura del botón
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.midnight_blue),
                    contentColor = colorResource(id = R.color.white)
                ),
                shape = RoundedCornerShape(12.dp), // Opcional: esquinas redondeadas
                enabled = username.isNotBlank() && password.isNotBlank() && state !is LoginState.Loading
            ) {
                if (state is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorResource(id = R.color.white)
                    )
                } else {
                    Text(
                        text = "Log in",
                        fontSize = 16.sp,           // Aquí va el fontSize
                        fontWeight = FontWeight.Medium  // Aquí va el fontWeight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 👇 Aquí el link de "Forgot your password?"
            TextButton(
                onClick = { /* TODO: Navegar a tu pantalla de recuperación */ },
                enabled = state !is LoginState.Loading
            ) {
                Text(
                    "Forgot your password?",
                    color = colorResource(id = R.color.primary_blue),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(id = R.color.primary_blue)
                    ),
                    enabled = state !is LoginState.Loading
                ) {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- MODAL DIALOG FOR LOGIN ERRORS ---
        var showLoginModal by remember { mutableStateOf(false) }
        var loginModalMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(state) {
            if (state is LoginState.Error) {
                loginModalMessage = (state as LoginState.Error).message
                showLoginModal = true
            } else {
                showLoginModal = false
            }
        }

        if (showLoginModal && loginModalMessage != null) {
            app.forku.presentation.common.components.AppModal(
                onDismiss = {
                    showLoginModal = false
                    viewModel.resetState()
                },
                onConfirm = {
                    showLoginModal = false
                    viewModel.resetState()
                },
                title = "Login Error",
                message = loginModalMessage ?: "Unknown error",
                confirmText = "OK",
                dismissText = ""
            )
        }

        if (state is LoginState.Loading) {
            LoadingOverlay()
        }
    }
}