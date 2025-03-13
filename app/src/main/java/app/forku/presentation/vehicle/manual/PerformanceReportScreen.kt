package app.forku.presentation.vehicle.manual

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen

@Composable
fun PerformanceReportScreen(
    navController: NavController,
    pdfUrl: String = "https://drive.google.com/file/d/1UXNMsRFqJH4fPfjtDvbvX-N57tGu-uUJ/preview",
    networkManager: NetworkConnectivityManager
) {
    BaseScreen(
        navController = navController,
        showTopBar = true,
        topBarTitle = "Performance Report",
        content = { padding ->
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        loadUrl(pdfUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        },
        networkManager = networkManager
    )
} 