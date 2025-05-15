package app.forku.presentation.user.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (User) -> Unit,
    networkManager: NetworkConnectivityManager,
    navController: NavController,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Reset state when entering screen
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // Handle login success
    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            val user = (state as LoginState.Success).user
            Log.d("LoginScreen", "Login successful, navigating to dashboard")
            // Ensure we're in a clean state before navigation
            tokenErrorHandler.resetAuthenticationState()
            onLoginSuccess(user)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
        ) {
            /* TODO: Handle back navigation */
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "fork U",
                color = Color(0xFFFFA726), // Orange color
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Login, get settled &\nlet's begin.",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFA726),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = Color(0xFFFFA726),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = Color(0xFFFFA726),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                enabled = state !is LoginState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFA726),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = Color(0xFFFFA726),
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = Color(0xFFFFA726),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                enabled = state !is LoginState.Loading,
                trailingIcon = {
                    TextButton(
                        onClick = { /* TODO: Handle forgot password */ },
                        enabled = state !is LoginState.Loading
                    ) {
                        Text("Forgot?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { 
                    Log.d("LoginScreen", "Login button clicked for user: $username")
                    viewModel.login(username, password)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA726),
                    contentColor = Color.Black
                ),
                enabled = username.isNotBlank() && password.isNotBlank() && state !is LoginState.Loading
            ) {
                if (state is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black
                    )
                } else {
                    Text("Log in")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        contentColor = Color(0xFFFFA726)
                    ),
                    enabled = state !is LoginState.Loading
                ) {
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (state is LoginState.Loading) {
            LoadingOverlay()
        }

        if (state is LoginState.Error) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                containerColor = Color.Red.copy(alpha = 0.8f),
                contentColor = Color.White
            ) {
                Text((state as LoginState.Error).message)
            }
        }
    }
}