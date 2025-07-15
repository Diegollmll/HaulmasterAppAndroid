package app.forku.presentation.scanner

import android.Manifest
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.forku.core.network.NetworkConnectivityManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import androidx.navigation.NavController
import app.forku.core.auth.TokenErrorHandler
import app.forku.presentation.common.components.BaseScreen
import app.forku.presentation.navigation.Screen

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    onNavigateToPreShiftCheck: (String) -> Unit,
    onNavigateToVehicleProfile: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QRScannerViewModel = hiltViewModel(),
    networkManager: NetworkConnectivityManager,
    navController: NavController,
    tokenErrorHandler: TokenErrorHandler
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    // Control flags for scanning and navigation
    var isScanning by remember { mutableStateOf(true) }
    var hasNavigated by remember { mutableStateOf(false) }
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var isProcessingNavigation by remember { mutableStateOf(false) }
    
    Log.d("QRFlow", "Screen State - isScanning: $isScanning, hasNavigated: $hasNavigated, isProcessingNavigation: $isProcessingNavigation")
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                Log.d("QRFlow", "Disposing camera resources")
                cameraProviderFuture.get()?.unbindAll()
                executor.shutdown()
            } catch (e: Exception) {
                Log.e("QRFlow", "Failed to cleanup camera resources", e)
            }
        }
    }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    val previewView = remember { PreviewView(context) }

    // Handle navigation events with protection against multiple navigations
    LaunchedEffect(state) {
        Log.d("QRFlow", "Navigation LaunchedEffect triggered - hasNavigated: $hasNavigated, isProcessingNavigation: $isProcessingNavigation")
        Log.d("QRFlow", "Current state: navigateToChecklist: ${state.navigateToChecklist}, vehicle: ${state.vehicle?.id}")
        
        if (!hasNavigated && !isProcessingNavigation) {
            state.vehicle?.id?.let { vehicleId ->
                if (state.navigateToChecklist) {
                    Log.d("QRFlow", "Starting navigation to Checklist with vehicleId: $vehicleId")
                    isProcessingNavigation = true
                    hasNavigated = true
                    navController.navigate("checklist/${vehicleId}") {
                        popUpTo("scanner") { inclusive = true }
                    }
                    Log.d("QRFlow", "Navigation command executed - hasNavigated: $hasNavigated")
                    // Reset the state after navigation
                    viewModel.resetNavigationState()
                }
            }
        }
    }

    // Reset scanning state when returning to this screen
    LaunchedEffect(Unit) {
        Log.d("QRFlow", "QRScanner screen entered/re-entered")
        isScanning = true
        hasNavigated = false
        isProcessingNavigation = false
        lastScannedCode = null
        viewModel.resetNavigationState()
    }

    // Reset scanning state when state.error is not null
    LaunchedEffect(state.error) {
        if (state.error != null) {
            Log.d("QRFlow", "Error detected, resetting states - Error: ${state.error}")
            isScanning = true
            hasNavigated = false
            isProcessingNavigation = false
            lastScannedCode = null
        }
    }

    // Request camera permission
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    BaseScreen(
        navController = navController,
        showTopBar = true,
        showBottomBar = false,
        showBackButton = true,
        topBarTitle = "Scan Vehicle QR",
        networkManager = networkManager,
        tokenErrorHandler = tokenErrorHandler,
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (cameraPermissionState.status.isGranted) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    ) { preview ->
                        cameraProviderFuture.addListener({
                            Log.d("QRFlow", "Setting up camera preview")
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build()
                            preview.setSurfaceProvider(previewView.surfaceProvider)

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                if (!isScanning || hasNavigated) {
                                    Log.d("QRFlow", "[ImageAnalyzer] Skipping analysis - isScanning: $isScanning, hasNavigated: $hasNavigated")
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                @androidx.camera.core.ExperimentalGetImage
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    Log.d("QRFlow", "[ImageAnalyzer] Processing new frame")
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    val scanner = BarcodeScanning.getClient()
                                    
                                    scanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty()) {
                                                Log.d("QRFlow", "[ImageAnalyzer] Found ${barcodes.size} barcodes")
                                                barcodes.firstOrNull()?.rawValue?.let { code ->
                                                    Log.d("QRFlow", "[ImageAnalyzer] QR Code detected: $code")
                                                    // Only process if this code hasn't been scanned before and we're not navigating
                                                    if (lastScannedCode != code && isScanning && !hasNavigated && !isProcessingNavigation) {
                                                        Log.d("QRFlow", "[ImageAnalyzer] Processing new QR code - lastScannedCode: $lastScannedCode")
                                                        lastScannedCode = code
                                                        isScanning = false
                                                        viewModel.onQrScanned(code)
                                                        Log.d("QRFlow", "[ImageAnalyzer] QR code processed - isScanning: $isScanning, hasNavigated: $hasNavigated")
                                                    } else {
                                                        Log.d("QRFlow", "[ImageAnalyzer] Skipping QR code - already processed or navigation in progress")
                                                    }
                                                }
                                            } else {
                                                Log.d("QRFlow", "[ImageAnalyzer] No barcodes found in frame")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("QRFlow", "[ImageAnalyzer] Error processing frame", e)
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    Log.d("QRFlow", "[ImageAnalyzer] No media image available")
                                    imageProxy.close()
                                }
                            }

                            try {
                                Log.d("QRFlow", "Binding camera lifecycle")
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("QRFlow", "Error binding camera lifecycle", e)
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(context))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        QRCodeFrame()
                    }
                } else {
                    Log.d("QRFlow", "Camera permission not granted")
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Camera permission is required to scan QR codes")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
                }

                if (state.error != null) {
                    Log.d("QRFlow", "Showing error toast: ${state.error}")
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    BackHandler {
        Log.d("QRFlow", "Back button pressed - isProcessingNavigation: $isProcessingNavigation")
        if (!isProcessingNavigation) {
            Log.d("QRFlow", "Executing back navigation")
            onNavigateBack()
        } else {
            Log.d("QRFlow", "Back navigation ignored - navigation in progress")
        }
    }
}

@Composable
private fun QRCodeFrame() {
    Box(
        modifier = Modifier
            .size(250.dp)
            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
    )
}