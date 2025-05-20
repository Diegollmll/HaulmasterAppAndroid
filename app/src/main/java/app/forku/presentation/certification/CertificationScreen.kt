package app.forku.presentation.certification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.common.components.ErrorScreen
import app.forku.presentation.common.components.LoadingOverlay
import app.forku.presentation.common.components.DatePickerDialog
import app.forku.core.utils.hideKeyboardOnTapOutside
import app.forku.core.utils.keyboardAwareScroll
import app.forku.core.auth.TokenErrorHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationScreen(
    viewModel: CertificationViewModel = hiltViewModel(),
    certificationId: String? = null,
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showIssueDatePicker by remember { mutableStateOf(false) }
    var showExpiryDatePicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(certificationId) {
        if (certificationId != null) {
            viewModel.loadCertification(certificationId)
        }
    }

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            navController.popBackStack()
        }
    }

    if (showIssueDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.updateIssueDate(date)
                showIssueDatePicker = false
            },
            onDismiss = { showIssueDatePicker = false }
        )
    }

    if (showExpiryDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.updateExpiryDate(date)
                showExpiryDatePicker = false
            },
            onDismiss = { showExpiryDatePicker = false }
        )
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = if (certificationId != null) "Edit Certification" else "Create Certification",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .navigationBarsPadding()
                    .hideKeyboardOnTapOutside()
            ) {
                when {
                    state.isLoading -> LoadingOverlay()
                    state.error != null -> ErrorScreen(
                        message = state.error!!,
                        onRetry = { viewModel.clearError() }
                    )
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                                .keyboardAwareScroll(scrollState)
                        ) {
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = { viewModel.updateName(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.updateDescription(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.issuer,
                                onValueChange = { viewModel.updateIssuer(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Issuer") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showIssueDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.issuedDate ?: "Select Issue Date")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showExpiryDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(state.expiryDate ?: "Select Expiry Date")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = state.certificationCode ?: "",
                                onValueChange = { viewModel.updateCertificationCode(it.replace("\n", " ").replace("\r", " ")) },
                                label = { Text("Certification Code") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.saveCertification() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                enabled = !state.isLoading && state.isValid
                            ) {
                                Text(if (certificationId != null) "Save Changes" else "Create Certification")
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 24.dp)
                            ) {
                                Text("Debug Info:", style = MaterialTheme.typography.titleSmall)
                                Text("isMarkedForDeletion: ${state.isMarkedForDeletion}")
                                Text("isDirty: ${state.isDirty}")
                                Text("isNew: ${state.isNew}")
                                Text("internalObjectId: ${state.internalObjectId}")
                            }
                        }
                    }
                }
            }
        }
    )
} 