/*
 * SPDX-FileCopyrightText: 2025 OmniAPK
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.details.composable

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aurora.store.R
import com.aurora.store.data.installer.InstallResult
import com.aurora.store.data.installer.XAPKInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "WebViewDownloader"

/**
 * Download state for WebView downloads
 */
sealed class WebDownloadState {
    data object Idle : WebDownloadState()
    data class Downloading(val progress: Int, val fileName: String) : WebDownloadState()
    data class Downloaded(val file: File) : WebDownloadState()
    data class Installing(val file: File) : WebDownloadState()
    data class Installed(val success: Boolean, val message: String?) : WebDownloadState()
    data class Error(val message: String) : WebDownloadState()
}

/**
 * Shared OkHttpClient with proper timeout configuration
 */
private val downloadClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .followRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
}

/**
 * Downloads a file from URL and installs it
 * @return Updated download state
 */
private suspend fun performDownloadAndInstall(
    context: Context,
    url: String,
    fileName: String,
    userAgent: String,
    cookies: String?,
    onProgress: (WebDownloadState) -> Unit
) {
    var file: File? = null
    
    try {
        val downloadDir = File(context.getExternalFilesDir(null), "alternative_downloads")
        if (!downloadDir.exists() && !downloadDir.mkdirs()) {
            onProgress(WebDownloadState.Error("Cannot create download directory"))
            return
        }
        
        // Generate unique filename to avoid collisions
        val uniqueFileName = "${UUID.randomUUID().toString().take(8)}_$fileName"
        file = File(downloadDir, uniqueFileName)
        
        onProgress(WebDownloadState.Downloading(0, fileName))
        
        val requestBuilder = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept", "*/*")
            .header("Accept-Encoding", "gzip, deflate, br")
        
        if (!cookies.isNullOrEmpty()) {
            requestBuilder.header("Cookie", cookies)
        }
        
        val response = withContext(Dispatchers.IO) {
            downloadClient.newCall(requestBuilder.build()).execute()
        }
        
        response.use { resp ->
            if (!resp.isSuccessful) {
                file?.delete()
                onProgress(WebDownloadState.Error("HTTP ${resp.code}: ${resp.message}"))
                return
            }
            
            val body = resp.body
            if (body == null) {
                file?.delete()
                onProgress(WebDownloadState.Error("Empty response"))
                return
            }
            
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L
            
            withContext(Dispatchers.IO) {
                body.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            
                            if (totalBytes > 0) {
                                val progress = ((downloadedBytes * 100) / totalBytes).toInt()
                                onProgress(WebDownloadState.Downloading(progress, fileName))
                            }
                        }
                    }
                }
            }
        }
        
        Log.i(TAG, "Download complete: ${file.absolutePath}")
        onProgress(WebDownloadState.Downloaded(file))
        
        // Auto-install after download
        onProgress(WebDownloadState.Installing(file))
        val installer = XAPKInstaller(context)
        val result = installer.installAuto(file)
        
        val finalState = when (result) {
            is InstallResult.Success -> WebDownloadState.Installed(true, null)
            is InstallResult.Error -> WebDownloadState.Installed(false, result.message)
        }
        onProgress(finalState)
        
    } catch (e: Exception) {
        Log.e(TAG, "Download failed", e)
        // Clean up partial file on error
        file?.let { 
            if (it.exists()) {
                it.delete()
                Log.d(TAG, "Cleaned up partial download: ${it.name}")
            }
        }
        onProgress(WebDownloadState.Error(e.message ?: "Download failed"))
    }
}

/**
 * Checks if the file is an APK-type file
 */
private fun isApkDownload(fileName: String, mimetype: String?): Boolean {
    // Primary check: file extension (most reliable)
    val validExtensions = listOf(".apk", ".xapk", ".apks", ".apkm")
    val hasValidExtension = validExtensions.any { fileName.endsWith(it, ignoreCase = true) }
    
    // Secondary check: MIME type
    val hasValidMimetype = mimetype?.let {
        it.contains("android.package", ignoreCase = true) ||
        it.contains("vnd.android", ignoreCase = true) ||
        it == "application/octet-stream" // Generic binary (common for APKs)
    } ?: false
    
    return hasValidExtension || hasValidMimetype
}

/**
 * WebView-based download dialog for APKMirror and APKPure
 * Allows users to browse the sites and intercepts download requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewDownloadDialog(
    source: String,
    packageName: String,
    appName: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    
    var downloadState by remember { mutableStateOf<WebDownloadState>(WebDownloadState.Idle) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var currentUrl by remember { mutableStateOf("") }
    var canGoBack by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Build the initial URL based on source
    // APKPure: Supports direct app page via /app/{packageName} format
    // APKMirror: Does not support direct package lookup, uses optimized search
    val initialUrl = remember(source, packageName) {
        when (source) {
            "APKMirror" -> "https://www.apkmirror.com/?post_type=app_release&searchtype=app&s=${packageName}"
            "APKPure" -> "https://apkpure.com/app/${packageName}"
            else -> "https://www.apkmirror.com/?post_type=app_release&searchtype=app&s=${packageName}"
        }
    }
    
    // Show download status dialog if downloading/installing
    when (val state = downloadState) {
        is WebDownloadState.Downloading,
        is WebDownloadState.Downloaded,
        is WebDownloadState.Installing,
        is WebDownloadState.Installed,
        is WebDownloadState.Error -> {
            DownloadStatusDialog(
                state = state,
                onDismiss = {
                    if (state is WebDownloadState.Installed || state is WebDownloadState.Error) {
                        downloadState = WebDownloadState.Idle
                        if (state is WebDownloadState.Installed && state.success) {
                            onDismiss()
                        }
                    }
                },
                onRetry = {
                    downloadState = WebDownloadState.Idle
                }
            )
        }
        else -> {}
    }
    
    AlertDialog(
        onDismissRequest = {
            if (downloadState == WebDownloadState.Idle) {
                webView?.destroy()
                onDismiss()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f),
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (canGoBack) {
                        IconButton(onClick = { webView?.goBack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back)
                            )
                        }
                    }
                    
                    Text(
                        text = "$source - $appName",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.action_refresh)
                        )
                    }
                    
                    IconButton(onClick = {
                        webView?.destroy()
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_close)
                        )
                    }
                }
                
                // Progress bar
                if (isLoading) {
                    LinearProgressIndicator(
                        progress = { pageProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        text = {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    createWebView(ctx, initialUrl) { url, fileName, userAgent, cookies ->
                        scope.launch {
                            performDownloadAndInstall(
                                context = ctx,
                                url = url,
                                fileName = fileName,
                                userAgent = userAgent,
                                cookies = cookies,
                                onProgress = { downloadState = it }
                            )
                        }
                    }.apply {
                        webView = this
                        
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                pageProgress = newProgress / 100f
                                isLoading = newProgress < 100
                            }
                        }
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                currentUrl = url ?: ""
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                canGoBack = view?.canGoBack() ?: false
                                currentUrl = url ?: ""
                            }
                        }
                    }
                },
                update = { view ->
                    canGoBack = view.canGoBack()
                }
            )
        },
        confirmButton = {},
        dismissButton = {}
    )
    
    // Cleanup WebView on dispose
    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }
}

/**
 * Creates a WebView configured for downloading APKs
 * JavaScript is required for APKMirror/APKPure functionality
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    initialUrl: String,
    onDownloadRequest: (url: String, fileName: String, userAgent: String, cookies: String?) -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Cookie settings for CloudFlare bypass
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)
        
        // WebView settings - JavaScript required for modern websites
        settings.apply {
            javaScriptEnabled = true  // Required for APKMirror/APKPure to work
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = false  // Security: disable file access
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW  // Security: block mixed content
            
            // Modern user agent based on device
            userAgentString = "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }
        
        // Intercept download requests
        setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
            Log.d(TAG, "Download requested: $url, mime: $mimetype, size: $contentLength")
            
            // Determine filename
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                ?: "download_${System.currentTimeMillis()}.apk"
            
            // Check if it's an APK file
            if (isApkDownload(fileName, mimetype)) {
                val cookies = CookieManager.getInstance().getCookie(url)
                onDownloadRequest(url, fileName, userAgent, cookies)
            } else {
                Log.w(TAG, "Non-APK download ignored: $fileName ($mimetype)")
            }
        }
        
        loadUrl(initialUrl)
    }
}

/**
 * Dialog showing download/install status
 */
@Composable
private fun DownloadStatusDialog(
    state: WebDownloadState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (state is WebDownloadState.Installed || state is WebDownloadState.Error) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = when (state) {
                    is WebDownloadState.Downloading -> stringResource(R.string.alternative_downloading)
                    is WebDownloadState.Downloaded -> stringResource(R.string.alternative_download_complete)
                    is WebDownloadState.Installing -> stringResource(R.string.alternative_installing)
                    is WebDownloadState.Installed -> if (state.success) {
                        stringResource(R.string.alternative_install_success)
                    } else {
                        stringResource(R.string.webview_install_failed)
                    }
                    is WebDownloadState.Error -> stringResource(R.string.webview_download_error)
                    else -> ""
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is WebDownloadState.Downloading -> {
                        Text(
                            text = state.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "%${state.progress}")
                    }
                    
                    is WebDownloadState.Downloaded,
                    is WebDownloadState.Installing -> {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.alternative_installing),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    is WebDownloadState.Installed -> {
                        if (state.success) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_cancel),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            if (state.message != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    
                    is WebDownloadState.Error -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_cancel),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    else -> {}
                }
            }
        },
        confirmButton = {
            when (state) {
                is WebDownloadState.Installed,
                is WebDownloadState.Error -> {
                    Button(onClick = onDismiss) {
                        Text(text = stringResource(R.string.action_close))
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            if (state is WebDownloadState.Error) {
                TextButton(onClick = onRetry) {
                    Text(text = stringResource(R.string.action_retry))
                }
            }
        }
    )
}
